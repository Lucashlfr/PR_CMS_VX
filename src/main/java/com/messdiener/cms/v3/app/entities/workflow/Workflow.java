package com.messdiener.cms.v3.app.entities.workflow;

import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Workflow {

    private UUID workflowId;
    private UUID ownerId;

    private WorkflowType workflowType;
    private CMSState CMSState;

    private CMSDate creationDate;
    private CMSDate endDate;

    private int currentNumber;
    private String metadata;

}
