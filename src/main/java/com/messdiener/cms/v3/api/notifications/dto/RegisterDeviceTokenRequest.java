// src/main/java/com/messdiener/cms/v3/api/notifications/dto/RegisterDeviceTokenRequest.java
package com.messdiener.cms.v3.api.notifications.dto;

public record RegisterDeviceTokenRequest(
        String token,
        String platform // ANDROID / IOS / WEB
) {}
