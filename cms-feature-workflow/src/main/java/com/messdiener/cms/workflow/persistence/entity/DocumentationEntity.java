package com.messdiener.cms.workflow.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_documentation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentationEntity {

    @Id
    @Column(name = "documentation_id", nullable = false, length = 36)
    private UUID documentationId; // Domain: documentationId :contentReference[oaicite:65]{index=65}

    @Column(name = "workflow_id", nullable = false, length = 36)
    private UUID workflowId; // Domain: workflowId :contentReference[oaicite:66]{index=66}

    @Column(name = "title", length = 255, nullable = false)
    private String title; // Domain: title :contentReference[oaicite:67]{index=67}

    @Lob @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description; // Domain: description :contentReference[oaicite:68]{index=68}

    @Column(name = "assignee_id", length = 36)
    private UUID assigneeId; // Domain: assigneeId :contentReference[oaicite:69]{index=69}

    @Column(name = "created_at", nullable = false)
    private Long createdAt; // Domain: CMSDate createdAt -> long :contentReference[oaicite:70]{index=70}
}
