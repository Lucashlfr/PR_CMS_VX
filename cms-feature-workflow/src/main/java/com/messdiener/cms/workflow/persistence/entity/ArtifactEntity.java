package com.messdiener.cms.workflow.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_artifacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArtifactEntity {

    @Id
    @Column(name = "artifact_id", nullable = false, length = 36)
    private UUID artifactId; // Domain: artifactId :contentReference[oaicite:53]{index=53}

    @Column(name = "workflow_id", nullable = false, length = 36)
    private UUID workflowId; // Domain: workflowId :contentReference[oaicite:54]{index=54}

    @Column(name = "artifact_type", length = 64, nullable = false)
    private String artifactType; // Domain: artifactType :contentReference[oaicite:55]{index=55}

    @Column(name = "created_at", nullable = false)
    private Long createdAt; // Domain: CMSDate createdAt -> long :contentReference[oaicite:56]{index=56}

    @Column(name = "created_by", length = 36)
    private UUID createdBy; // Domain: createdBy :contentReference[oaicite:57]{index=57}
}
