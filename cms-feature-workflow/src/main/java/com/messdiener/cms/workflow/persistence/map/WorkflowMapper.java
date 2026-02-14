// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\map\WorkflowMapper.java
package com.messdiener.cms.workflow.persistence.map;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowCategory;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import com.messdiener.cms.workflow.persistence.entity.WorkflowEntity;

import java.util.Optional;
import java.util.UUID;

public final class WorkflowMapper {

    private WorkflowMapper() {}

    public static Workflow toDomain(WorkflowEntity e) {
        WorkflowCategory category = Optional.ofNullable(e.getWorkflowCategory()).orElse(WorkflowCategory.ONB);
        CMSState state = Optional.ofNullable(e.getWorkflowState()).orElse(CMSState.ACTIVE);

        UUID editor = e.getEditor();
        UUID creator = e.getCreator() != null ? e.getCreator() : editor;
        UUID manager = e.getManager() != null ? e.getManager() : editor;
        UUID target  = e.getTarget()  != null ? e.getTarget()  : editor;

        int attachments = e.getAttachments() != null ? e.getAttachments() : 0;
        int notes       = e.getNotes() != null ? e.getNotes() : 0;

        long creation = Optional.ofNullable(e.getCreationDate()).orElse(System.currentTimeMillis());
        long modified = Optional.ofNullable(e.getModificationDate()).orElse(creation);
        long end      = Optional.ofNullable(e.getEndDate()).orElse(0L);

        return new Workflow(
                e.getWorkflowId(),
                e.getWorkflowName(),              // String -> String
                e.getWorkflowDescription(),
                category,
                state,
                editor,
                creator,
                manager,
                target,
                attachments,
                notes,
                CMSDate.of(creation),
                CMSDate.of(modified),
                CMSDate.of(end),
                Optional.ofNullable(e.getCurrentNumber()).orElse(0),
                Optional.ofNullable(e.getMetaData()).orElse("")
        );
    }

    public static WorkflowEntity toEntity(Workflow d) {
        return WorkflowEntity.builder()
                .workflowId(d.getWorkflowId())
                .workflowName(d.getWorkflowName()) // String -> String
                .workflowDescription(d.getWorkflowDescription())
                .workflowCategory(d.getCategory())
                .workflowState(d.getState())
                .editor(d.getEditor())
                .creator(d.getCreator())
                .manager(d.getManager())
                .target(d.getTarget())
                .attachments(d.getAttachments())
                .notes(d.getNotes())
                .creationDate(d.getCreationDate() != null ? d.getCreationDate().toLong() : System.currentTimeMillis())
                .modificationDate(d.getModificationDate() != null ? d.getModificationDate().toLong() : System.currentTimeMillis())
                .endDate(d.getEndDate() != null ? d.getEndDate().toLong() : 0L)
                .currentNumber(d.getCurrentNumber())
                .metaData(d.getMetadata())
                .build();
    }
}
