package com.messdiener.cms.workflow.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import com.messdiener.cms.workflow.persistence.service.FormService;
import com.messdiener.cms.workflow.persistence.service.WorkflowCreationService;
import com.messdiener.cms.workflow.persistence.service.WorkflowService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WorkflowFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowFormController.class);
    private final SecurityHelper securityHelper;
    private final WorkflowService workflowService;
    private final PersonHelper personHelper;
    private final FormService workflowModuleService;
    private final AuditService auditService;
    private final PersonService personService;
    private final FormService formService;
    private final WorkflowCreationService workflowCreationService;

    @PostConstruct
    public void init() {
        LOGGER.info("WorkflowFormController initialized.");
    }

    @GetMapping("/workflow/dashboard")
    public String workflowDashboard(HttpSession httpSession, Model model) {
        PersonSessionView sessionUser = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("forms", formService.getActiveFormsForUser(user.getId()));
        model.addAttribute("personHelper", personHelper);

        model.addAttribute("user", user);

        return "workflow/list/workflow_dashboard";
    }

    @GetMapping("/workflow/form")
    public String state(HttpSession httpSession, Model model, @RequestParam("id") UUID id) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        securityHelper.addPersonToSession(httpSession);
        WorkflowForm module = workflowModuleService.getWorkflowModuleById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkflowModule not found"));

        module.preScript();

        model.addAttribute("module", module);
        model.addAttribute("user", user);
        model.addAttribute("components", module.getComponents());

        if (module.getFLevel().equals("INFORMATION")) {
            return "workflow/pages/infopage";
        }

        if (module.getUniqueName() == WorkflowFormName.SCHEDULER) {
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
            if (f > user.getFRank()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing Permissions");
            }
        }

        return "workflow/forms/workflow_popup";
    }

    @PostMapping("/workflow/upload")
    public ResponseEntity<Map<String, String>> handleUpload(
            HttpServletRequest request,
            @RequestParam("module") UUID moduleId,
            @RequestParam Map<String, String> params,
            @RequestParam(required = false) MultipartFile fileUpload
    ) throws JsonProcessingException, SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        if (fileUpload != null && !fileUpload.isEmpty()) {
            LOGGER.info("Hochgeladene Datei: {}", fileUpload.getOriginalFilename());
        }

        List<Map<String, String>> result = params.entrySet().stream().map(e -> Map.of("name", e.getKey(), "value", e.getValue())).toList();

        ObjectMapper mapper = new ObjectMapper();
        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);

        WorkflowForm module = workflowModuleService.getWorkflowModuleById(moduleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkflowModule not found"));
        module.setResult(jsonOutput);
        module.setState(CMSState.COMPLETED);
        workflowModuleService.saveForm(module);

        module.postScript();

        if(module.getUniqueName() != WorkflowFormName.APPROVAL){
            workflowCreationService.createApproval(module);
        }

        return ResponseEntity.ok(Map.of("redirectUrl", "/workflow/dashboard"));
    }

    @GetMapping("/workflow/module/closePage")
    public String closeModulePage() {
        return "close-popup";
    }

    @GetMapping("/workflow/jump")
    public RedirectView jumpPage(@RequestParam("id") UUID workflowId) {
        UUID moduleId = workflowModuleService.getCurrentModuleId(workflowId);
        return new RedirectView("/workflow/module?id=" + moduleId + "&wf=" + workflowId);
    }
}
