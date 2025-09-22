package com.messdiener.cms.workflow.domain.list;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.ElementType;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class WorkflowListElement {

    private UUID elementId;
    private ElementType elementType;
    private int number;

    private UUID assigneeId;
    private CMSDate createdAt;
    private CMSDate modifiedAt;

    private String title;
    private String description;
    private CMSState state;
}
