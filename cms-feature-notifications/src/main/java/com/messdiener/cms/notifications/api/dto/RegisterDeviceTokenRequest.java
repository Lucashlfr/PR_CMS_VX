package com.messdiener.cms.notifications.api.dto;

// src/main/java/com/messdiener/cms/v3/api/notifications/dto/RegisterDeviceTokenRequest.java
public record RegisterDeviceTokenRequest(
        String token,
        String platform // ANDROID / IOS / WEB
) {}
