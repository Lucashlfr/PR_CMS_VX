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
import com.messdiener.cms.workflow.domain.dto.WorkflowDTO;
import com.messdiener.cms.workflow.persistence.repo.AutomationRuleRepository;
import com.messdiener.cms.workflow.persistence.repo.FormDefinitionRepository;
import com.messdiener.cms.workflow.persistence.repo.ProcessDefinitionRepository;
import com.messdiener.cms.workflow.persistence.repo.WorkflowNotificationTemplateRepository;
import com.messdiener.cms.workflow.persistence.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WorkflowController {

    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;

    // Neue Services (V2 + Subressourcen)
    private final WorkflowServiceV2 workflowService;
    private final FormSubmissionService submissionService;
    private final DocumentationService documentationService;
    private final WorkflowTaskService taskService;
    private final WorkflowNotificationService notificationService;
    private final NotificationTemplateService templateService;

    private final AuditService auditService;
    private final PersonService personService;
    private final AutomationRuleRepository automationRuleRepository;
    private final FormDefinitionRepository formDefinitionRepository;
    private final WorkflowNotificationTemplateRepository workflowNotificationTemplateRepository;
    private final ProcessDefinitionRepository processDefinitionRepository;

    @PostConstruct
    public void init() {
        log.info("WorkflowController (V2) initialized.");
    }

    @GetMapping("/workflow")
    public String workflows(
            HttpSession httpSession,
            Model model,
            @RequestParam("s") Optional<String> s,
            @RequestParam("q") Optional<String> qs,
            @RequestParam("id") Optional<String> idS
    ) {
        // Security / User laden
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("htmlClasses", new HTMLClasses());
        model.addAttribute("personHelper", personHelper);

        final String step = s.orElse("me");
        model.addAttribute("step", step);

        final String query = qs.orElse("null");

         if("admin".equalsIgnoreCase(step)) {
             // immer für Admin-Tab befüllen (oder bedingt, wenn step.orElse("").equals("admin"))
             model.addAttribute("automationRules", automationRuleRepository.findAll());
             model.addAttribute("formDefinitions", formDefinitionRepository.findAll());
             model.addAttribute("notificationTemplates", workflowNotificationTemplateRepository.findAll());
             model.addAttribute("processDefinitions", processDefinitionRepository.findAll());

             return "workflow/interface/admin";
        }

        // Detailansicht
        if ("info".equalsIgnoreCase(query) && idS.isPresent()) {
            UUID workflowId = UUID.fromString(idS.get());

            WorkflowDTO workflow = workflowService.getById(workflowId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));

            model.addAttribute("workflow", workflow);

            // Optionale Sub-Ressourcen für die Detailseite (wenn deine View sie nutzt)
            model.addAttribute("submissions", submissionService.getByWorkflow(workflowId));
            model.addAttribute("documents", documentationService.getByWorkflow(workflowId));
            model.addAttribute("tasks", taskService.getByWorkflow(workflowId));
            model.addAttribute("notifications", notificationService.getByTemplate(null) /* leer → in View filtern */);
            model.addAttribute("audit", auditService.getLogsByConnectId(workflowId));

            // Wenn du Prozessschritte/Module weiterhin anzeigen willst und eine Methode dafür hast:
            //model.addAttribute("steps", workflowModuleService.getWorkflowModulesByWorkflowId(workflowId));

            return "workflow/interface/workflowInterface";
        }

        // Listenansicht
        List<WorkflowDTO> workflows;
        Map<String, Integer> stateCountMap;

        if ("all".equalsIgnoreCase(step)) {
            // Kein Tenants-Getter in V2 → wir aggregieren alle States und deduplizieren
            workflows = Stream.of(CMSState.values())
                    .flatMap(st -> workflowService.getByState(st).stream())
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(WorkflowDTO::workflowId, w -> w, (a, b) -> a, LinkedHashMap::new),
                            m -> new ArrayList<>(m.values())
                    ));
        } else if ("me".equalsIgnoreCase(step)) {
            // „Meine“ = mir zugewiesen ∪ von mir beantragt
            var assigned = workflowService.getByAssignee(user.getId());
            var applied = workflowService.getByApplicant(user.getId());
            workflows = Stream.concat(assigned.stream(), applied.stream())
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(WorkflowDTO::workflowId, w -> w, (a, b) -> a, LinkedHashMap::new),
                            m -> new ArrayList<>(m.values())
                    ));
        } else if ("create".equalsIgnoreCase(step)) {
            // Types direkt aus dem Enum (statt nicht vorhandenen workflowTypeService)
            model.addAttribute("types", WorkflowType.values());

            // Personenliste analog zu deiner bestehenden Logik:
            model.addAttribute("persons",
                    personService.getActivePersonsByPermissionDTO(user.getFRank(), user.getTenant()));

            // neue Strukturen bereitstellen
            model.addAttribute("processDefinitions", processDefinitionRepository.findAll());
            model.addAttribute("formDefinitions", formDefinitionRepository.findAll());

            return "workflow/interface/createWorkflow";
        }
        else {
            workflows = Collections.emptyList();
        }

        // Zähler pro State (aus der angezeigten Liste, nicht DB-weite Counts)
        stateCountMap = Arrays.stream(CMSState.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        st -> (int) workflows.stream().filter(w -> st.equals(w.state())).count(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        fillCommonModel(model, workflows, user);
        model.addAttribute("counter", stateCountMap);

        return "workflow/interface/workflowOverview";
    }

    // -----------------------------------------------------

    private void fillCommonModel(Model model, List<WorkflowDTO> workflows, Person user) {
        model.addAttribute("workflows", workflows);
        model.addAttribute("state", CMSState.class);
        model.addAttribute("types", WorkflowType.values());
        model.addAttribute("persons",
                personService.getActivePersonsByPermissionDTO(user.getFRank(), user.getTenant()));
    }
}
