package com.messdiener.cms.notifications.api.dto;

import java.util.UUID;

public record SendNotificationRequest(
        UUID userId,
        String title,
        String body,
        String dataJson
) {}
