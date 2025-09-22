package com.messdiener.cms.workflow.persistence.map;

import com.messdiener.cms.workflow.domain.dto.NotificationTemplateDTO;
import com.messdiener.cms.workflow.persistence.entity.NotificationTemplateEntity;

public final class WorkflowNotificationTemplateMapper {
    private WorkflowNotificationTemplateMapper() {}

    public static NotificationTemplateDTO toDto(NotificationTemplateEntity e) {
        return new NotificationTemplateDTO(
                e.getNotificationTemplateId(),
                e.getKey(),
                e.getChannel(),
                e.getVersion() != null ? e.getVersion() : 1,
                e.getSubjectTemplate(),
                e.getBodyTemplate()
        );
    }

    public static NotificationTemplateEntity toEntity(NotificationTemplateDTO d) {
        return NotificationTemplateEntity.builder()
                .notificationTemplateId(d.notificationTemplateId())
                .key(d.key())
                .channel(d.channel())
                .version(d.version())
                .subjectTemplate(d.subjectTemplate())
                .bodyTemplate(d.bodyTemplate())
                .build();
    }
}
