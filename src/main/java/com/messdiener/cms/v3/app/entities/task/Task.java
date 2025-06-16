package com.messdiener.cms.v3.app.entities.task;

import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Task {

    private UUID taskId;
    private int number;
    private UUID link;

    private UUID creator;
    private UUID editor;

    private CMSDate lastUpdate;
    private CMSDate creationDate;
    private CMSDate endDate;

    private String title;
    private String description;

    private CMSState taskState;
    private String priority;


}
