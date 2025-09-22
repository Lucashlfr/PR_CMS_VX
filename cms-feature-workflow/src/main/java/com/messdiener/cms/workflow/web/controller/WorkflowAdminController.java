package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.shared.enums.notfication.CMSNotification;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.workflow.persistence.entity.AutomationRuleEntity;
import com.messdiener.cms.workflow.persistence.entity.FormDefinitionEntity;
import com.messdiener.cms.workflow.persistence.entity.NotificationTemplateEntity;
import com.messdiener.cms.workflow.persistence.entity.ProcessDefinitionEntity;
import com.messdiener.cms.workflow.persistence.repo.AutomationRuleRepository;
import com.messdiener.cms.workflow.persistence.repo.FormDefinitionRepository;
import com.messdiener.cms.workflow.persistence.repo.ProcessDefinitionRepository;
import com.messdiener.cms.workflow.persistence.repo.WorkflowNotificationTemplateRepository;
import com.messdiener.cms.workflow.persistence.service.WorkflowServiceV2;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WorkflowAdminController {

    private final SecurityHelper securityHelper;

    // Repositories (richtige Bean-Namen aus deinem Projekt)
    private final AutomationRuleRepository automationRuleRepo;
    private final FormDefinitionRepository formDefinitionRepo;
    private final ProcessDefinitionRepository processDefinitionRepo;
    private final WorkflowNotificationTemplateRepository notificationTemplateRepo;

    // Service für das eigentliche Anlegen von Workflows
    private final WorkflowServiceV2 workflowService;

    // ==========================
    //  CREATE: Automatisierung
    // ==========================
    @PostMapping("/workflow/config/create/automation")
    public RedirectView createAutomation(
            @RequestParam Optional<UUID> id,
            @RequestParam String name,
            @RequestParam String triggerType,
            @RequestParam(required = false) String triggerConfig,
            @RequestParam(required = false) String conditionJson,
            @RequestParam(required = false) String actionsJson,
            @RequestParam(defaultValue = "false") boolean enabled
    ) {
        requireUser();

        AutomationRuleEntity entity = AutomationRuleEntity.builder()
                .ruleId(id.orElse(UUID.randomUUID()))
                .name(name)
                .triggerType(triggerType)
                .triggerConfig(triggerConfig)
                .conditionJson(conditionJson)
                .actionsJson(actionsJson)
                .enabled(enabled)
                .build();

        automationRuleRepo.save(entity);
        log.info("Created AutomationRule: {} ({})", entity.getName(), entity.getRuleId());
        return new RedirectView("/workflow?s=admin");
    }

    // =======================
    //  CREATE: FormDefinition
    // =======================
    @PostMapping("/workflow/config/create/form")
    public RedirectView createForm(
            @RequestParam Optional<UUID> id,
            @RequestParam String key,
            @RequestParam @Min(1) int version,
            @RequestParam String formName,
            @RequestParam(required = false) String formDescription,
            @RequestParam(required = false) String formImg,
            @RequestParam(required = false) String jsonSchema,
            @RequestParam(required = false) String uiSchema,
            @RequestParam(required = false) String validationsJson,
            @RequestParam(required = false) String outputTemplatesJson
    ) {
        requireUser();

        FormDefinitionEntity entity = FormDefinitionEntity.builder()
                .formDefinitionId(id.orElse(UUID.randomUUID()))
                .key(key)
                .version(version)
                .formName(formName)
                .formDescription(formDescription)
                .formImg(formImg)
                .jsonSchema(jsonSchema)
                .uiSchema(uiSchema)
                .validationsJson(validationsJson)
                .outputTemplatesJson(outputTemplatesJson)
                .build();

        formDefinitionRepo.save(entity);
        log.info("Created FormDefinition: {} v{}", entity.getKey(), entity.getVersion());
        return new RedirectView("/workflow?s=admin");
    }

    // ==========================
    //  CREATE: NotificationTemplate
    // ==========================
    @PostMapping("/workflow/config/create/notification")
    public RedirectView createNotification(
            @RequestParam Optional<UUID> id,
            @RequestParam String key,
            @RequestParam String channel, // z.B. "EMAIL" | "PUSH" | "SMS"
            @RequestParam @Min(1) int version,
            @RequestParam(required = false) String subjectTemplate,
            @RequestParam(required = false) String bodyTemplate,
            @RequestParam(required = false) String jsonSchema
    ) {
        requireUser();

        NotificationTemplateEntity entity = NotificationTemplateEntity.builder()
                .notificationTemplateId(id.orElse(UUID.randomUUID()))
                .key(key)
                .channel(CMSNotification.valueOf(channel))
                .version(version)
                .subjectTemplate(subjectTemplate)
                .bodyTemplate(bodyTemplate)
                .build();

        notificationTemplateRepo.save(entity);
        log.info("Created NotificationTemplate: {} [{}] v{}", entity.getKey(), entity.getChannel(), entity.getVersion());
        return new RedirectView("/workflow?s=admin");
    }

    // ========================
    //  CREATE: ProcessDefinition
    // ========================
    @PostMapping("/workflow/config/create/process")
    public RedirectView createProcess(
            @RequestParam Optional<UUID> id,
            @RequestParam String key,
            @RequestParam @Min(1) int version,
            @RequestParam String name,
            @RequestParam(required = false) String statesJson,
            @RequestParam(required = false) String transitionsJson,
            @RequestParam(required = false) String slaPoliciesJson
    ) {
        requireUser();

        ProcessDefinitionEntity entity = ProcessDefinitionEntity.builder()
                .processDefinitionId(id.orElse(UUID.randomUUID()))
                .key(key)
                .version(version)
                .name(name)
                .statesJson(statesJson)
                .transitionsJson(transitionsJson)
                .slaPoliciesJson(slaPoliciesJson)
                .build();

        processDefinitionRepo.save(entity);
        log.info("Created ProcessDefinition: {} v{}", entity.getKey(), entity.getVersion());
        return new RedirectView("/workflow?s=admin");
    }

    // ====================
    //  POST: /workflow/create
    //  - legt einen Workflow über WorkflowServiceV2 an
    // ====================
    @PostMapping("/workflow/create")
    public RedirectView createWorkflow(
            @RequestParam("type") String type,               // dein Enum-Name als String (z.B. WorkflowType)
            @RequestParam("person") String personSelector,   // "me"|"all"|"tenant"|UUID
            @RequestParam(value = "label", required = false) String label,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            @RequestParam("processDefinitionId") UUID processDefinitionId,
            @RequestParam(value = "initialState", required = false) String initialState,
            @RequestParam(value = "formDefinitionIds", required = false) List<UUID> formDefinitionIds,
            @RequestParam(value = "enableAutomation", defaultValue = "false") boolean enableAutomation
    ) {
        PersonSessionView sessionUser = requireUser();

        ProcessDefinitionEntity process = processDefinitionRepo.findById(processDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ProcessDefinition not found"));

        List<FormDefinitionEntity> forms = (formDefinitionIds == null || formDefinitionIds.isEmpty())
                ? List.of()
                : formDefinitionRepo.findAllById(formDefinitionIds);

        // Delegiere die eigentliche Anlage an deinen Service (Signatur nach deinem Service anpassen!)
        UUID workflowId = workflowService.createWorkflow(
                type,                   // z. B. Enum-Name
                personSelector,         // "me"/"all"/"tenant"/UUID
                label,
                description,
                startDate,
                endDate,
                process,
                initialState,
                forms,
                enableAutomation,
                sessionUser.id()
        );

        return new RedirectView("/workflow?s=detail&id=" + workflowId);
    }

    // ======
    // Utils
    // ======
    private PersonSessionView requireUser() {
        return securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
