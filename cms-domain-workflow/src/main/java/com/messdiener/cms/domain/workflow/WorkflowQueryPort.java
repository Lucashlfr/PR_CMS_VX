package com.messdiener.cms.domain.workflow;

import java.util.List;
import java.util.UUID;

public interface WorkflowQueryPort {
    int countRelevantWorkflows(String personId);

    List<WorkflowSummaryDTO> getWorkflowsByUserId(UUID userId);
}
