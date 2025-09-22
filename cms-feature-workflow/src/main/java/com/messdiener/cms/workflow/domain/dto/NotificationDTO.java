package com.messdiener.cms.workflow.domain.dto;

import com.messdiener.cms.shared.enums.workflow.CMSState;

import java.util.Map;
import java.util.UUID;

public record NotificationDTO(
        UUID notificationId,
        UUID templateId,
        CMSState state,
        Map<String, Object> payload
) {}
