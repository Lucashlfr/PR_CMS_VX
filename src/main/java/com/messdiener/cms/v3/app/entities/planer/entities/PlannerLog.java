package com.messdiener.cms.v3.app.entities.planer.entities;

import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
import com.messdiener.cms.v3.shared.enums.tasks.LogType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
@Data
public class
PlannerLog {

    private UUID logId;
    private PlanerTag tag;
    private LogType logType;
    private CMSDate date;
    private String headline;
    private String logText;

}
