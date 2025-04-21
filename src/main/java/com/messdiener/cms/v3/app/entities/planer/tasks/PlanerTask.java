package com.messdiener.cms.v3.app.entities.planer.tasks;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.tasks.TaskPriority;
import com.messdiener.cms.v3.shared.enums.tasks.TaskState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PlanerTask {

    private UUID taskId;
    private int taskNumber;

    private String taskName;
    private String taskDescription;

    private TaskState taskState;
    private TaskPriority priority;

    private CMSDate created;
    private CMSDate updated;

    private String lable;

    private List<TaskMessage> messageList;

    public static PlanerTask createPlanerTask(String taskName, String taskDescription, String lable) throws SQLException {
        return new PlanerTask(UUID.randomUUID(), 0, taskName, taskDescription, TaskState.REDIRECTED, TaskPriority.NULL, CMSDate.current(), CMSDate.current(), lable, new ArrayList<>());
    }

}
