package com.messdiener.cms.workflow.domain.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class Workflow {

    private UUID workflowId;

    private String processKey;
    private int processVersion;

    private String title;
    private String description;

    private CMSState state;

    private UUID assigneeId;
    private UUID applicantId;
    private UUID manager;
    private UUID targetElement;

    private int priority;
    private List<String> tags;

    private int attachments;
    private int notes;

    private CMSDate creationDate;
    private CMSDate modificationDate;
    private CMSDate endDate;

    private int currentNumber;
    private String metadata;

}
