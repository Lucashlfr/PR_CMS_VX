package com.messdiener.cms.task.domain.entity.task;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private UUID taskId;
    private int number;

    private UUID link;       // z. B. Event-ID oder generischer Verweis
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
