package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowTaskEntity {

    @Id
    @Column(name = "task_id", nullable = false, length = 36)
    private UUID id; // Domain: id :contentReference[oaicite:83]{index=83}

    @Column(name = "workflow_id", nullable = false, length = 36)
    private UUID workflowId; // Domain: workflowId :contentReference[oaicite:84]{index=84}

    @Column(name = "task_key", length = 255, nullable = false)
    private String key; // Domain: key :contentReference[oaicite:85]{index=85}

    @Column(name = "title", length = 255, nullable = false)
    private String title; // Domain: title :contentReference[oaicite:86]{index=86}

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 64, nullable = false)
    private CMSState state; // Domain: state :contentReference[oaicite:87]{index=87}

    @Lob @Column(name = "candidate_roles_json", columnDefinition = "LONGTEXT")
    private String candidateRolesJson; // Domain: List<String> candidateRoles -> JSON :contentReference[oaicite:88]{index=88}

    @Column(name = "assignee_id", length = 36)
    private UUID assigneeId; // Domain: assigneeId :contentReference[oaicite:89]{index=89}

    @Column(name = "due_at")
    private Long dueAt; // Domain: CMSDate dueAt -> long :contentReference[oaicite:90]{index=90}

    @Column(name = "created_at", nullable = false)
    private Long createdAt; // Domain: CMSDate createdAt -> long :contentReference[oaicite:91]{index=91}

    @Lob @Column(name = "payload_json", columnDefinition = "LONGTEXT")
    private String payloadJson; // Domain: Map payload -> JSON :contentReference[oaicite:92]{index=92}
}
