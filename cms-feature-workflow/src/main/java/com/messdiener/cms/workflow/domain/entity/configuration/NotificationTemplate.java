package com.messdiener.cms.workflow.domain.entity.configuration;

import java.util.UUID;

import com.messdiener.cms.shared.enums.notfication.CMSNotification;
import lombok.*;
@Data
@AllArgsConstructor
public class NotificationTemplate {

    private UUID notificationTemplateId;
    private String key;

    private CMSNotification channel;
    private int version;

    private String subjectTemplate;
    private String bodyTemplate;

}

