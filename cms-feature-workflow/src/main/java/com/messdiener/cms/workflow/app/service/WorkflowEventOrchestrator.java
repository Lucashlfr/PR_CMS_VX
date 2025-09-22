package com.messdiener.cms.workflow.app.service;

import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.FormSubmissionDTO;
import com.messdiener.cms.workflow.persistence.service.DocumentationService;
import com.messdiener.cms.workflow.persistence.service.WorkflowAuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowEventOrchestrator {

    private final WorkflowTaskApplicationService taskApp;
    private final WorkflowNotificationApplicationService notificationApp;
    private final DocumentationService documentationService;
    private final WorkflowAuditTrailService auditService;

    public void onSubmissionCreated(FormSubmissionDTO submission) {
        auditService.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                submission.workflowId(),
                null,
                "FormSubmission created",
                "Form " + submission.formKey() + " v" + submission.formVersion() + " submitted (state=" + submission.state() + ")"
        ));

        taskApp.createManualTask(
                submission.workflowId(),
                "Submission prüfen: " + submission.formKey(),
                CMSState.OPEN,
                null,
                null,
                Map.of("formKey", submission.formKey(), "submissionId", submission.id())
        );

        notificationApp.notifyWorkflow(
                submission.workflowId(),
                "Neue Formular-Abgabe",
                "Für den Workflow wurde eine neue Abgabe erstellt: " + submission.formKey()
        );
    }

    public void onSubmissionCompleted(UUID workflowId, String reason) {
        auditService.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                workflowId,
                null,
                "Workflow completed",
                (reason == null ? "Workflow abgeschlossen" : reason)
        ));

        notificationApp.notifyWorkflow(
                workflowId,
                "Workflow abgeschlossen",
                "Der Workflow wurde erfolgreich abgeschlossen."
        );
    }

    public void addDocumentation(UUID workflowId, String title, String description, UUID assignee) {
        documentationService.save(new com.messdiener.cms.workflow.domain.dto.DocumentationDTO(
                UUID.randomUUID(),
                workflowId,
                title,
                description,
                assignee,
                CMSDate.current()
        ));
    }
}
