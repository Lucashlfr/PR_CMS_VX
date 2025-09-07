// src/main/java/com/messdiener/cms/v3/api/notifications/dto/SendNotificationRequest.java
package com.messdiener.cms.v3.api.notifications.dto;

import java.util.UUID;

public record SendNotificationRequest(
        UUID userId,
        String title,
        String body,
        String dataJson
) {}
