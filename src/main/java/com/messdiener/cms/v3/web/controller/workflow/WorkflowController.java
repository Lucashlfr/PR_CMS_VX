package com.messdiener.cms.v3.web.controller.workflow;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.workflow.Workflow;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.helper.workflow.WorkflowHelper;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowModuleService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.v3.utils.html.HTMLClasses;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WorkflowController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowController.class);
    private final SecurityHelper securityHelper;
    private final WorkflowService workflowService;
    private final PersonHelper personHelper;
    private final WorkflowHelper workflowHelper;
    private final WorkflowModuleService workflowModuleService;
    private final AuditService auditService;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        LOGGER.info("WorkflowController initialized.");
    }


    @GetMapping("/workflow")
    public String workflows(HttpSession httpSession, Model model, @RequestParam("s") Optional<String> s, @RequestParam("q") Optional<String> qs, @RequestParam("id") Optional<String> idS) throws SQLException {
        Person user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("htmlClasses", new HTMLClasses());

        model.addAttribute("personHelper", personHelper);

        String step = s.orElse("me");
        model.addAttribute("step", step);

        String query = qs.orElse("null");

        if (query.equals("info") && idS.isPresent()) {
            UUID workflowId = UUID.fromString(idS.get());
            Workflow workflow = workflowService.getWorkflowById(workflowId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
            model.addAttribute("workflow", workflow);
            model.addAttribute("steps", workflowModuleService.getWorkflowModulesByWorkflowId(workflowId));
            model.addAttribute("audit", auditService.getLogsByConnectId(workflowId));
            return "workflow/interface/workflowInterface";
        }

        List<Workflow> workflows = new ArrayList<>();
        Map<String, Integer> stateCountMap = new HashMap<>();
        if(step.equals("all")){
            workflows =  workflowService.getAllWorkflowsByTenant(user.getTenantId());
            stateCountMap = workflowService.countWorkflowStatesByTenant(user.getTenantId());

        }else if(step.equals("me")){
            workflows = workflowService.getWorkflowsByUserId(user.getId());
            stateCountMap = workflowService.countWorkflowStates(user.getId());
        }
        model.addAttribute("workflows", workflows);
        model.addAttribute("counter", stateCountMap);
        model.addAttribute("state", CMSState.class);

        model.addAttribute("types", WorkflowType.values());
        model.addAttribute("persons", personService.getActivePersonsByPermissionDTO(user.getFRank(),user.getTenantId()));
        if(step.equals("create"))
            return "workflow/interface/createWorkflow";
        return "workflow/interface/workflowOverview";
    }

    @GetMapping("/workflow/dashboard")
    public String workflowDashboard(HttpSession httpSession, Model model) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("workflows", workflowService.getRelevantWorkflows(user.getId().toString()));
        model.addAttribute("workflowHelper", workflowHelper);
        model.addAttribute("user", user);

        return "workflow/list/workflow_dashboard";
    }

    @GetMapping("/workflow/module")
    public String state(Model model, @RequestParam("id") UUID id, @RequestParam("wf") UUID wf) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        WorkflowModule module = workflowModuleService.getWorkflowModuleById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkflowModule not found"));
        module.preScript();

        model.addAttribute("module", module);

        model.addAttribute("user", user);

        Workflow workflow = workflowService.getWorkflowById(wf).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
        model.addAttribute("workflow", workflow);
        model.addAttribute("id", workflow.getWorkflowId());

        model.addAttribute("steps", workflowModuleService.getWorkflowModulesByWorkflowId(wf));

        model.addAttribute("components", module.getComponents());

        if (module.getFLevel().equals("INFORMATION")) {
            return "workflow/pages/infopage";
        }

        if(module.getUniqueName() == WorkflowModuleName.SCHEDULER){
            model.addAttribute("connections", personHelper.getConnections(user));
            return "workflow/pages/workflow_scheduler";
        }

        if (!module.getFLevel().equals("SELF")) {
            int f;
            try {
                f = Integer.parseInt(module.getFLevel().replace("F", ""));
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid F level");
            }

            if (f > user.getFRank())
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing Permissions");
        }

        if (module.getNumber() != workflow.getCurrentNumber())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Number of workflows do not match");

        return "workflow/forms/workflow_popup";
    }

    @PostMapping("/workflow/upload")
    public ResponseEntity<Map<String, String>> handleUpload(HttpServletRequest request,
                                                            @RequestParam("workflow") UUID workflowId,
                                                            @RequestParam("module") UUID moduleId,
                                                            @RequestParam Map<String, String> params,
                                                            @RequestParam(required = false) MultipartFile fileUpload) throws JsonProcessingException, SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Workflow workflow = workflowService.getWorkflowById(workflowId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));

        // Optional: Dateiname
        if (fileUpload != null && !fileUpload.isEmpty()) {
            System.out.println("Hochgeladene Datei: " + fileUpload.getOriginalFilename());
        }

        // Name/Value-Paare als JSON-Array aufbauen
        List<Map<String, String>> result = params.entrySet().stream()
                .map(e -> Map.of("name", e.getKey(), "value", e.getValue()))
                .collect(Collectors.toList());

        // In JSON umwandeln (mit Jackson)
        ObjectMapper mapper = new ObjectMapper();
        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);


        WorkflowModule module = workflowModuleService.getWorkflowModuleById(moduleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkflowModule not found"));
        module.setResult(jsonOutput);
        workflowModuleService.saveWorkflowModule(workflowId, module);

        module.postScript();

        workflowHelper.nextStep(module, workflow);

        String redirectUrl = workflowHelper.createUrl(user, module, workflow);
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }

    @GetMapping("/workflow/module/closePage")
    public String closeModulePage() {
        return "close-popup";
    }


    @GetMapping("/workflow/jump")
    public RedirectView jumpPage(@RequestParam("id") UUID workflowId) throws SQLException {

        UUID moduleId = workflowModuleService.getCurrentModuleId(workflowId);

        return new RedirectView("/workflow/module?id=" + moduleId + "&wf=" + workflowId);
    }

    @PostMapping("/workflow/create")
    public RedirectView create(@RequestParam("type")WorkflowType type, @RequestParam("person")String person, @RequestParam("startDate")Optional<String> startDateE, @RequestParam("endDate")Optional<String> endDateE) throws SQLException {
        Person user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        List<PersonOverviewDTO> target = new ArrayList<>();

        if(person.equals("me")){
            target.add(user.toPersonOverviewDTO());
        }else if(person.equals("all")){
            target.addAll(personService.getActivePersonsByPermissionDTO(user.getFRank(),user.getTenantId()));
        }else if(person.equals("tenant")){
            target.addAll(personService.getActiveMessdienerByTenantDTO(user.getTenantId()));
        }else {
            target.add(personService.getPersonById(UUID.fromString(person)).orElseThrow().toPersonOverviewDTO());
        }
        workflowHelper.createWorkflow(target, type, startDateE, endDateE);

        return new RedirectView("/workflow");
    }

    @PostMapping("/workflow/scheduler/submit")
    public RedirectView handleEventSubmission(@RequestParam("workflowId") UUID workflowId, @RequestParam("module") UUID moduleId,
                                              @RequestParam Map<String, String> eventOptions) throws SQLException {
        Person user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Workflow workflow = workflowService.getWorkflowById(workflowId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
        WorkflowModule module = workflowModuleService.getWorkflowModuleById(moduleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkflowModule not found"));

        for (Map.Entry<String, String> entry : eventOptions.entrySet()) {
            String key = entry.getKey();
            if ("module".equals(key) || "workflowId".equals(key)) {
                continue;
            }
            UUID eventId = UUID.fromString(entry.getKey().replace("event_", ""));
            LiturgieState state = Integer.parseInt(entry.getValue()) == 0 ? LiturgieState.UNAVAILABLE : LiturgieState.AVAILABLE;

            //organisationMappingService.setMapState(eventId, user.getId(), state);
        }

        workflowHelper.nextStep(module, workflow);
        return new RedirectView("/workflow/jump?id=" + workflowId);
    }

}
