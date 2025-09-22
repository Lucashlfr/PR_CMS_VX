package com.messdiener.cms.workflow.domain.dto;

import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

public record DocumentationDTO(
        UUID documentationId,
        UUID workflowId,
        String title,
        String description,
        UUID assigneeId,
        CMSDate createdAt
) {}
