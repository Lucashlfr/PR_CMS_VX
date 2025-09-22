// cms-feature-workflow/src/main/java/com/messdiener/cms/workflow/persistence/entity/WorkflowEntity.java
package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_workflows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowEntity {

    @Id
    @Column(name = "workflow_id", nullable = false, length = 36)
    private UUID workflowId; // Domain: workflowId :contentReference[oaicite:4]{index=4}

    @Column(name = "process_key", length = 255, nullable = false)
    private String processKey; // Domain: processKey :contentReference[oaicite:5]{index=5}

    @Column(name = "process_version", nullable = false)
    private Integer processVersion; // Domain: processVersion :contentReference[oaicite:6]{index=6}

    @Column(name = "title", length = 255, nullable = false)
    private String title; // Domain: title :contentReference[oaicite:7]{index=7}

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description; // Domain: description :contentReference[oaicite:8]{index=8}

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 64, nullable = false)
    private CMSState state; // Domain: state :contentReference[oaicite:9]{index=9}

    @Column(name = "assignee_id", length = 36)
    private UUID assigneeId; // Domain: assigneeId :contentReference[oaicite:10]{index=10}

    @Column(name = "applicant_id", length = 36)
    private UUID applicantId; // Domain: applicantId :contentReference[oaicite:11]{index=11}

    @Column(name = "manager_id", length = 36)
    private UUID manager; // Domain: manager :contentReference[oaicite:12]{index=12}

    @Column(name = "target_element", length = 36)
    private UUID targetElement; // Domain: targetElement :contentReference[oaicite:13]{index=13}

    @Column(name = "priority", nullable = false)
    private Integer priority; // Domain: priority :contentReference[oaicite:14]{index=14}

    @Lob
    @Column(name = "tags_json", columnDefinition = "LONGTEXT")
    private String tagsJson; // Domain: List<String> tags -> JSON :contentReference[oaicite:15]{index=15}

    @Column(name = "attachments", nullable = false)
    private Integer attachments; // Domain: attachments :contentReference[oaicite:16]{index=16}

    @Column(name = "notes", nullable = false)
    private Integer notes; // Domain: notes :contentReference[oaicite:17]{index=17}

    @Column(name = "creation_date")
    private Long creationDate; // Domain: CMSDate creationDate -> long :contentReference[oaicite:18]{index=18}

    @Column(name = "modification_date")
    private Long modificationDate; // Domain: CMSDate modificationDate -> long :contentReference[oaicite:19]{index=19}

    @Column(name = "end_date")
    private Long endDate; // Domain: CMSDate endDate -> long :contentReference[oaicite:20]{index=20}

    @Column(name = "current_number", nullable = false)
    private Integer currentNumber; // Domain: currentNumber :contentReference[oaicite:21]{index=21}

    @Lob
    @Column(name = "metadata", columnDefinition = "LONGTEXT")
    private String metadata; // Domain: metadata (frei) :contentReference[oaicite:22]{index=22}
}
