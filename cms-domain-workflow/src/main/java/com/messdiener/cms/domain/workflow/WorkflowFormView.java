package com.messdiener.cms.domain.workflow;

import com.messdiener.cms.shared.enums.workflow.CMSState;

import java.util.UUID;

public record WorkflowFormView(
        UUID moduleId,
        UUID workflowId,
        String uniqueName,
        String name,
        String description,
        String img,
        CMSState state,
        int number,
        String creationDate,
        String modificationDate
) {
}
