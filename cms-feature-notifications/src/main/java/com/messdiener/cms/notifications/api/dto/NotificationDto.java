package com.messdiener.cms.notifications.api.dto;

// src/main/java/com/messdiener/cms/v3/api/notifications/dto/NotificationDto.java
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
