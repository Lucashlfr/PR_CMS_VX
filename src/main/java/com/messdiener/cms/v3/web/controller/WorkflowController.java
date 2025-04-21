package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.entities.workflows.WorkflowLog;
import com.messdiener.cms.v3.app.entities.workflows.request.WorkflowRequest;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.shared.enums.StatusState;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.shared.enums.tasks.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.tasks.MessageType;
import com.messdiener.cms.v3.shared.enums.tasks.TaskState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WorkflowController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowController.class);
    private final Cache cache;
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("WorkflowController initialized.");
    }

    @GetMapping("/workflows")
    public String workflows(HttpSession session, Model model, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> id) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        model.addAttribute("user", user);
        securityHelper.addPersonToSession(session);

        String query = q.orElse("null");
        switch (query) {
            case "edit" -> {
                if (id.isPresent()) {
                    UUID workflowId = UUID.fromString(id.get());
                    Workflow workflow = cache.getWorkflowService().getWorkflow(workflowId, user.getId()).orElseThrow();
                    model.addAttribute("workflow", workflow);

                    if (workflow.getWorkflowType() == WorkflowAttributes.WorkflowType.SCHEDULER) {
                        model.addAttribute("events", cache.getOrganisationEventService().getNextEvents(user.getTenantId(), OrganisationType.WORSHIP));
                        model.addAttribute("connections", personHelper.getConnections(user));
                        return "workflows/pages/workflow_scheduler";
                    }
                }
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            case "createScheduler" -> {
                checkPermission(user, "TEAM");
                model.addAttribute("persons", cache.getPersonService().getActivePersonsByPermission(user.getId(), "*", user.getTenantId())); //TODO: FIX
                model.addAttribute("workflowType", WorkflowAttributes.WorkflowType.SCHEDULER.name());
                model.addAttribute("tenantName", personHelper.getTenantName(user).orElse(""));
                return "workflows/interface/create_workflow";
            }
            case "current" -> {
                checkPermission(user, "TEAM");
                model.addAttribute("workflows", cache.getWorkflowQueryService().getWorkflowsSummary());

                if (id.isPresent()) {
                    UUID workflowId = UUID.fromString(id.get());
                    Workflow workflow = cache.getWorkflowService().getWorkflow(workflowId).orElseThrow();
                    model.addAttribute("workflow", workflow);
                    model.addAttribute("workflows", cache.getWorkflowService().getWorkflows(workflowId));
                    return "workflows/workflowStates";
                }

                return "workflows/currentWorkflows";
            }
        }

        model.addAttribute("workflows", cache.getWorkflowQueryService().getWorkflowsByUser(user.getId(), WorkflowAttributes.WorkflowState.PENDING));
        return "workflows/workflowIndex";
    }

    @PostMapping("/workflow/create")
    public ResponseEntity<String> createWorkflow(@RequestBody WorkflowRequest request) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start- und Enddatum müssen angegeben werden.");
        }

        if (request.getUuids() == null || request.getUuids().isEmpty()) {
            return ResponseEntity.badRequest().body("Mindestens eine UUID muss ausgewählt sein.");
        }

        UUID workflowId = UUID.randomUUID();
        WorkflowAttributes.WorkflowType workflowType = WorkflowAttributes.WorkflowType.valueOf(request.getType());

        CMSDate creation = CMSDate.current();
        CMSDate start = CMSDate.convert(request.getStartDate(), DateUtils.DateType.ENGLISH);
        CMSDate end = CMSDate.convert(request.getEndDate(), DateUtils.DateType.ENGLISH);

        WorkflowLog log = new WorkflowLog(UUID.randomUUID(), workflowId, creation, user.getId(), "Workflow erstellt", user.getName() + " hat den Workflow erstellt");

        for (String uuid : request.getUuids()) {
            UUID personId = UUID.fromString(uuid);
            Workflow workflow = new Workflow(workflowId, user.getTenantId(), personId, workflowType,
                    WorkflowAttributes.WorkflowState.PENDING, creation, start, end, creation, user.getId(), List.of(log), 0, 0);
            cache.getWorkflowService().createWorkflow(workflow);
            cache.getGlobalManager().startUp();
        }

        return ResponseEntity.ok("Workflow erstellt für " + request.getUuids().size() + " Personen.");
    }

    @PostMapping("/workflow/scheduler/submit")
    public RedirectView handleEventSubmission(@RequestParam("id") UUID workflowId, @RequestParam Map<String, String> eventOptions) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Workflow workflow = cache.getWorkflowService().getWorkflow(workflowId, user.getId()).orElseThrow();

        for (Map.Entry<String, String> entry : eventOptions.entrySet()) {
            if (entry.getValue().contains(workflowId.toString())) continue;
            UUID eventId = UUID.fromString(entry.getKey().replace("event_", ""));
            int selected = Integer.parseInt(entry.getValue());

            cache.getOrganisationMappingService().setMapState(eventId, user.getId(), selected, 0, 0);
        }

        workflow.setWorkflowState(WorkflowAttributes.WorkflowState.COMPLETED);
        cache.getWorkflowService().updateWorkflow(workflow);

        Task task = cache.getTaskQueryService().getNormedTaskById(user.getId(), "WF_" + workflow.getCreationDate().getDate()).orElseThrow();
        TaskMessage taskMessage = new TaskMessage(UUID.randomUUID(), MessageType.ENDE, "Workflow abgeschlossen", "", MessageInformationCascade.C0, CMSDate.current(), Optional.of(user.getId()), false);
        cache.getTaskMessageService().saveMessage(task.getTaskId(), taskMessage);
        task.setTaskState(TaskState.COMPLETED);
        task.setEndDate(Optional.of(CMSDate.current()));
        task.setUpdateDate(CMSDate.current());
        cache.getTaskService().saveTask(task);

        return new RedirectView("/workflows?statusState=" + StatusState.SCHEDULER_OK);
    }

    private void checkPermission(Person user, String permission) throws SQLException {
        if (!personHelper.hasPermission(user, permission)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Zugriff verweigert für: " + permission);
        }
    }
}
