package com.messdiener.cms.workflow.domain.dto;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.List;
import java.util.UUID;

public record WorkflowDTO(
        UUID workflowId,
        String processKey,
        int processVersion,
        String title,
        String description,
        CMSState state,
        UUID assigneeId,
        UUID applicantId,
        UUID manager,
        UUID targetElement,
        int priority,
        List<String> tags,
        int attachments,
        int notes,
        CMSDate creationDate,
        CMSDate modificationDate,
        CMSDate endDate,
        int currentNumber,
        String metadata
) {}
