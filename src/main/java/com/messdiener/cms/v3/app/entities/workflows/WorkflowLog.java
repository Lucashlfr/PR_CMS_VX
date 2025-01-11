package com.messdiener.cms.v3.app.entities.workflows;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class WorkflowLog {

    private UUID logId;
    private UUID workflowId;
    private CMSDate date;
    private UUID creatorId;
    private String title;
    private String description;

}
