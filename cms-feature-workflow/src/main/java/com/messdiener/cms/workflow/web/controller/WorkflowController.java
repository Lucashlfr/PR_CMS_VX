package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.shared.utils.html.HTMLClasses;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import com.messdiener.cms.workflow.persistence.service.FormService;
import com.messdiener.cms.workflow.persistence.service.WorkflowService;
import com.messdiener.cms.workflow.web.view.TimelineService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class WorkflowController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowController.class);
    private final SecurityHelper securityHelper;
    private final WorkflowService workflowService;
    private final PersonHelper personHelper;
    private final FormService workflowModuleService;
    private final AuditService auditService;
    private final PersonService personService;
    private final TimelineService timelineService;

    @PostConstruct
    public void init() {
        LOGGER.info("WorkflowController initialized.");
    }

    @GetMapping("/workflow")
    public String workflows(HttpSession httpSession, Model model, @RequestParam("s") Optional<String> s, @RequestParam("q") Optional<String> qs, @RequestParam("id") Optional<String> idS) {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("htmlClasses", new HTMLClasses());
        model.addAttribute("personHelper", personHelper);

        String step = s.orElse("me");
        model.addAttribute("step", step);

        String query = qs.orElse("null");

        if (query.equals("info") && idS.isPresent()) {
            UUID workflowId = UUID.fromString(idS.get());
            Workflow workflow = workflowService.getWorkflowById(workflowId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
            model.addAttribute("workflow", workflow);
            model.addAttribute("steps", workflowModuleService.getWorkflowModulesByWorkflowId(workflowId));
            model.addAttribute("audit", auditService.getLogsByConnectId(workflowId));

            model.addAttribute("timeline", timelineService.buildForWorkflow(workflowId));

            return "workflow/interface/workflowInterface";
        }

        List<Workflow> workflows;
        Map<String, Integer> stateCountMap;
        if (step.equals("all")) {
            workflows = workflowService.getAllWorkflowsByTenant(user.getTenant());
            stateCountMap = workflowService.countWorkflowStatesByTenant(user.getTenant());
        } else if (step.equals("me")) {
            workflows = workflowService.getWorkflowsByUserId(user.getId());
            stateCountMap = workflowService.countWorkflowStates(user.getId());
        } else {
            workflows = new ArrayList<>();
            stateCountMap = new HashMap<>();
        }

        model.addAttribute("workflows", workflows);
        model.addAttribute("counter", stateCountMap);
        model.addAttribute("state", CMSState.class);
        model.addAttribute("types", WorkflowType.values());
        model.addAttribute("persons",
                personService.getActivePersonsByPermissionDTO(user.getFRank(), user.getTenant()));

        if (step.equals("create")) {
            return "workflow/interface/createWorkflow";
        }
        return "workflow/interface/workflowOverview";
    }
}
