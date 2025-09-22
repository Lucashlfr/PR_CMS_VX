package com.messdiener.cms.workflow.persistence.map;

import com.messdiener.cms.workflow.domain.dto.NotificationDTO;
import com.messdiener.cms.workflow.persistence.entity.NotificationEntity;

public final class WorkflowNotificationMapper {
    private WorkflowNotificationMapper() {}

    public static NotificationDTO toDto(NotificationEntity e) {
        return new NotificationDTO(
                e.getNotificationId(),
                e.getTemplateId(),
                e.getState(),
                Jsons.toMap(e.getPayloadJson())
        );
    }

    public static NotificationEntity toEntity(NotificationDTO d) {
        return NotificationEntity.builder()
                .notificationId(d.notificationId())
                .templateId(d.templateId())
                .state(d.state())
                .payloadJson(Jsons.toJson(d.payload()))
                .build();
    }
}
