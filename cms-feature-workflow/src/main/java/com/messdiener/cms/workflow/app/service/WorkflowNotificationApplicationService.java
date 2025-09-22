package com.messdiener.cms.workflow.app.service;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.domain.dto.NotificationDTO;
import com.messdiener.cms.workflow.persistence.service.WorkflowNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowNotificationApplicationService {

    private final WorkflowNotificationService notificationService;

    public void notifyWorkflow(UUID workflowId, String title, String body) {
        // Generische Workflow-Notification: wir verwenden ein "generisches" TemplateId-Feld = workflowId
        var dto = new NotificationDTO(
                UUID.randomUUID(),
                workflowId,              // als Template/Context-Anker
                CMSState.OPEN,           // neu angelegt -> OPEN
                Map.of("title", title, "body", body, "scope", "workflow")
        );
        notificationService.save(dto);
    }

    public void notifyUser(UUID userId, String title, String body) {
        var dto = new NotificationDTO(
                UUID.randomUUID(),
                userId,                  // als Template/Context-Anker (Empfänger)
                CMSState.OPEN,
                Map.of("title", title, "body", body, "scope", "user")
        );
        notificationService.save(dto);
    }
}
