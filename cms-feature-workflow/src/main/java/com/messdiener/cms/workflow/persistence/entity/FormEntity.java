// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\entity\WorkflowModuleEntity.java
package com.messdiener.cms.workflow.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_workflow_forms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormEntity {

    @Id
    @Column(name = "moduleId", length = 36, nullable = false)
    private UUID moduleId;

    @Column(name = "workflowId", length = 36, nullable = false)
    private UUID workflowId;

    @Column(name = "creatorId", length = 36)
    private UUID creatorId;

    @Column(name = "currentId", length = 36)
    private UUID currentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflowState", length = 255, nullable = false)
    private CMSState state;

    @Column(name = "creationDate")
    private Long creationDate;

    @Column(name = "modificationDate")
    private Long modificationDate;

    @Column(name = "number")
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "uniqueName", length = 255, nullable = false)
    private WorkflowFormName uniqueName;

    @Lob
    @Column(name = "metaData")
    private String metaData;
}
