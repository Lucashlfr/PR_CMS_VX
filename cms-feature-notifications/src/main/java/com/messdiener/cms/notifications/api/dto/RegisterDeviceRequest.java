package com.messdiener.cms.notifications.api.dto;

public record RegisterDeviceRequest(
        String token,
        String platform,   // "android" | "ios"
        String appVersion,
        String language
) {}