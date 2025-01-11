package com.messdiener.cms.v3.app.entities.workflows;

import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
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

    public void create() throws SQLException {
        Cache.getWorkflowService().createWorkflow(this);
    }

    public void complete() throws SQLException {
        this.setWorkflowState(WorkflowAttributes.WorkflowState.COMPLETED);
        this.setLastUpdateDate(CMSDate.current());
        Cache.getWorkflowService().updateWorkflow(this);
    }
}
