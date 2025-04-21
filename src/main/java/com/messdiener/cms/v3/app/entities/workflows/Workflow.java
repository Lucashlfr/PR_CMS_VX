package com.messdiener.cms.v3.app.entities.workflows;

import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@AllArgsConstructor
@Data
public class Workflow {

    private UUID workflowId;
    private UUID tenantId;
    private UUID userId;
    private WorkflowAttributes.WorkflowType workflowType;
    private WorkflowAttributes.WorkflowState workflowState;

    private CMSDate creationDate;
    private CMSDate startDate;
    private CMSDate endDate;
    private CMSDate lastUpdateDate;

    private UUID creatorId;

    public List<WorkflowLog> logs;

    private int counter;
    private int completedCounter;
}
