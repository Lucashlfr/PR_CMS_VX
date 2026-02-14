package com.messdiener.cms.api.sso.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cms.sso")
public record SsoProperties(
        String secret,     // DEV-Secret via application-dev.yml
        int ttlSeconds,    // Link-Gültigkeit in Sekunden
        String webBaseUrl  // Basis-URL des Webfrontends (mit Protokoll)
) {}
