package com.messdiener.cms.domain.workflow;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

/**
 * Schlanke Projektions-DTO für Workflow-Übersichten.
 * Wird im Adapter aus WorkflowDTO + PersonHelper befüllt.
 */
public record WorkflowSummaryDTO(
        UUID workflowId,
        String title,            // statt workflowName
        String description,      // statt workflowDescription
        String processKey,       // statt WorkflowCategory enum
        CMSState state,
        String assigneeName,     // statt editor
        String applicantName,    // statt creator
        String managerName,
        int attachments,
        int notes,
        CMSDate creationDate,
        CMSDate modificationDate,
        CMSDate endDate,
        int currentNumber
) {
}
