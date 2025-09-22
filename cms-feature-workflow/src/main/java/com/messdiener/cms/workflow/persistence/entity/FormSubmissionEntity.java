// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\entity\FormSubmissionEntity.java
package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_form_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormSubmissionEntity {

    @Id
    @Column(name = "submission_id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "workflow_id", nullable = false, length = 36)
    private UUID workflowId;

    @Column(name = "form_key", length = 255, nullable = false)
    private String formKey;

    @Column(name = "form_version", nullable = false)
    private Integer formVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 64, nullable = false)
    private CMSState state; // <-- NEU

    @Column(name = "submitted_by", length = 36)
    private UUID submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private Long submittedAt;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Lob
    @Column(name = "payload_json", columnDefinition = "LONGTEXT")
    private String payloadJson;
}
