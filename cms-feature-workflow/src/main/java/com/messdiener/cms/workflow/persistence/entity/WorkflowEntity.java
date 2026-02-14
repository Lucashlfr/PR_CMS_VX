// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\entity\WorkflowEntity.java
package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_workflow")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowEntity {

    @Id
    @Column(name = "workflowId", length = 36, nullable = false)
    private UUID workflowId;

    @Column(name = "workflowName", length = 255)
    private String workflowName; // <-- früher WorkflowType, jetzt String

    @Lob
    @Column(name = "workflowDescription", columnDefinition = "LONGTEXT")
    private String workflowDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflowCategory", length = 255)
    private WorkflowCategory workflowCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflowState", length = 255)
    private CMSState workflowState;

    @Column(name = "workflowEditor", length = 36)
    private UUID editor;

    @Column(name = "workflowCreator", length = 36)
    private UUID creator;

    @Column(name = "workflowManager", length = 36)
    private UUID manager;

    @Column(name = "workflowTarget", length = 36)
    private UUID target;

    @Column(name = "attachments")
    private Integer attachments;

    @Column(name = "notes")
    private Integer notes;

    @Column(name = "creationDate")
    private Long creationDate;

    @Column(name = "modificationDate")
    private Long modificationDate;

    @Column(name = "endDate")
    private Long endDate;

    @Column(name = "currentNumber")
    private Integer currentNumber;

    @Lob
    @Column(name = "metaData", columnDefinition = "LONGTEXT")
    private String metaData;

}
