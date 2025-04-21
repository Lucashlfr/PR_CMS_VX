package com.messdiener.cms.v3.app.entities.tasks.entitites;

import com.messdiener.cms.v3.shared.enums.tasks.LogType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class TaskLog {

    private UUID logId;
    private UUID userId;
    private LogType logType;
    private CMSDate date;
    private String log;

}
