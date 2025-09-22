package com.messdiener.cms.workflow.domain.entity.data;

import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Documentation {

    private UUID documentationId;
    private UUID workflowId;

    private String title;
    private String description;

    private UUID assigneeId;

    private CMSDate createdAt;
}
