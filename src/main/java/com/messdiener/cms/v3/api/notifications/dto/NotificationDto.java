// src/main/java/com/messdiener/cms/v3/api/notifications/dto/NotificationDto.java
package com.messdiener.cms.v3.api.notifications.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String title,
        String body,
        String dataJson,
        Instant createdAt,
        Instant readAt
) {}
