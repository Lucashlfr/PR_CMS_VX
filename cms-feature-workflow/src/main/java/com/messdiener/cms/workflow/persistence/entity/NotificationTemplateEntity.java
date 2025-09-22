package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.notfication.CMSNotification;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_notification_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationTemplateEntity {

    @Id
    @Column(name = "notification_template_id", nullable = false, length = 36)
    private UUID notificationTemplateId; // :contentReference[oaicite:40]{index=40}

    @Column(name = "template_key", length = 255, nullable = false)
    private String key; // :contentReference[oaicite:41]{index=41}

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 64, nullable = false)
    private CMSNotification channel; // :contentReference[oaicite:42]{index=42}

    @Column(name = "version", nullable = false)
    private Integer version; // :contentReference[oaicite:43]{index=43}

    @Lob @Column(name = "subject_template", columnDefinition = "LONGTEXT")
    private String subjectTemplate; // :contentReference[oaicite:44]{index=44}

    @Lob @Column(name = "body_template", columnDefinition = "LONGTEXT")
    private String bodyTemplate; // :contentReference[oaicite:45]{index=45}
}
