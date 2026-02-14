// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\map\WorkflowModuleMapper.java
package com.messdiener.cms.workflow.persistence.map;

import com.messdiener.cms.shared.enums.workflow.*;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import com.messdiener.cms.workflow.persistence.entity.FormEntity;

import java.util.Optional;
import java.util.UUID;

public final class FormMapper {

    private FormMapper() {}

    public static WorkflowForm toDomain(FormEntity e) {
        CMSState status = Optional.ofNullable(e.getState()).orElse(CMSState.OPEN);
        WorkflowFormName unique  = Optional.ofNullable(e.getUniqueName()).orElse(WorkflowFormName.DATA);
        CMSDate creationDate = CMSDate.of(Optional.ofNullable(e.getCreationDate()).orElse(0L));
        CMSDate modificationDate = CMSDate.of(Optional.ofNullable(e.getModificationDate()).orElse(0L));
        String meta = Optional.ofNullable(e.getMetaData()).orElse("");
        UUID current  = e.getCurrentId();
        UUID creator = e.getCreatorId();

        return WorkflowForm.getWorkflowModule(e.getWorkflowId(), unique, e.getModuleId(), status,
                Optional.ofNullable(e.getNumber()).orElse(0), creationDate, modificationDate, meta, current, creator);
    }

    public static FormEntity toEntity(WorkflowForm d) {
        return FormEntity.builder()
                .moduleId(d.getModuleId())
                .workflowId(d.getWorkflowId())
                .currentId(d.getCurrentUser())
                .creatorId(d.getCreator())
                .state(d.getState())
                .creationDate(d.getCreationDate().toLong())
                .modificationDate(d.getModificationDate().toLong())
                .number(d.getNumber())
                .uniqueName(d.getUniqueName())
                .metaData(d.getResults())
                .build();
    }
}
