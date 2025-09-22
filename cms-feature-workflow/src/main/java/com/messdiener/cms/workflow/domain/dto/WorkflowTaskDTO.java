package com.messdiener.cms.workflow.domain.dto;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WorkflowTaskDTO(
        UUID id,
        UUID workflowId,
        String key,
        String title,
        CMSState state,
        List<String> candidateRoles,
        UUID assigneeId,
        CMSDate dueAt,
        CMSDate createdAt,
        Map<String, Object> payload
) {}
