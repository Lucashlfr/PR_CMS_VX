package com.messdiener.cms.notifications.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "notifications.fcm")
public class FcmProperties {
    /** Schaltet den FCM-Gateway an/aus. */
    private boolean enabled = false;

    /** Pfad zur Service-Account-JSON, z. B. file:config/centralmanagementsystemx-04cd9f4980ad.json */
    private Resource credentials;

    /** Optional explizit setzen; sonst wird’s aus den Credentials abgeleitet. */
    private String projectId;
}
