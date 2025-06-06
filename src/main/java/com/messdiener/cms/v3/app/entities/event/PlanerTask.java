package com.messdiener.cms.v3.app.entities.event;

import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlanerTask {

    private UUID taskId;
    private int taskNumber;

    private String taskName;
    private String taskDescription;

    private CMSState state;

    private CMSDate created;
    private CMSDate updated;

    private String lable;

    public static PlanerTask createPlanerTask(String taskName, String taskDescription, String lable) {
        return new PlanerTask(UUID.randomUUID(), 0, taskName, taskDescription, CMSState.ACTIVE, CMSDate.current(), CMSDate.current(), lable);
    }

}
