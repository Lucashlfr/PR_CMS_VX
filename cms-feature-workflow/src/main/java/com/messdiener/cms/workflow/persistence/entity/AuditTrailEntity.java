package com.messdiener.cms.workflow.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_audit_trails")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditTrailEntity {

    @Id
    @Column(name = "trail_id", nullable = false, length = 36)
    private UUID trailId; // Domain: trailId :contentReference[oaicite:58]{index=58}

    @Column(name = "workflow_id", nullable = false, length = 36)
    private UUID workflowId; // Domain: workflowId :contentReference[oaicite:59]{index=59}

    @Column(name = "actor_id", length = 36)
    private UUID actorId; // Domain: actorId :contentReference[oaicite:60]{index=60}

    @Column(name = "action", length = 255, nullable = false)
    private String action; // Domain: action :contentReference[oaicite:61]{index=61}

    @Lob @Column(name = "before_payload", columnDefinition = "LONGTEXT")
    private String beforePayloadJson; // Domain: beforePayload -> JSON :contentReference[oaicite:62]{index=62}

    @Lob @Column(name = "after_payload", columnDefinition = "LONGTEXT")
    private String afterPayloadJson; // Domain: afterPayload -> JSON :contentReference[oaicite:63]{index=63}

    @Column(name = "date", nullable = false)
    private Long date; // Domain: CMSDate date -> long :contentReference[oaicite:64]{index=64}
}
