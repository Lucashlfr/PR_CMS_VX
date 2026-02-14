package com.messdiener.cms.domain.workflow;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowCategory;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

public record WorkflowSummaryDTO(
        UUID workflowId,
        String workflowName,
        String workflowDescription,
        WorkflowCategory category,
        CMSState state,
        String editor,
        String creator,
        String manager,
        int attachments,
        int notes,
        CMSDate creationDate,
        CMSDate modificationDate,
        CMSDate endDate,
        int currentNumber
) {
}
