package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEntity {

    @Id
    @Column(name = "notification_id", nullable = false, length = 36)
    private UUID notificationId; // Domain: notificationId :contentReference[oaicite:79]{index=79}

    @Column(name = "template_id", length = 36, nullable = false)
    private UUID templateId; // Domain: templateId :contentReference[oaicite:80]{index=80}

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 64, nullable = false)
    private CMSState state; // Domain: state :contentReference[oaicite:81]{index=81}

    @Lob @Column(name = "payload_json", columnDefinition = "LONGTEXT")
    private String payloadJson; // Domain: payload -> JSON :contentReference[oaicite:82]{index=82}
}
