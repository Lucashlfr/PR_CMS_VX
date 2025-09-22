package com.messdiener.cms.workflow.domain.dto;

import com.messdiener.cms.shared.enums.notfication.CMSNotification;

import java.util.UUID;

public record NotificationTemplateDTO(
        UUID notificationTemplateId,
        String key,
        CMSNotification channel,
        int version,
        String subjectTemplate,
        String bodyTemplate
) {}
