package com.messdiener.cms.v3.app.entities.tasks;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.tasks.TaskCategory;
import com.messdiener.cms.v3.shared.enums.tasks.TaskPriority;
import com.messdiener.cms.v3.shared.enums.tasks.TaskState;
import com.messdiener.cms.v3.shared.enums.tasks.TaskType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Task {

   private UUID taskId;
   private int taskNumber;
   private String normedTaskName;

   private String taskTitle;
   private String taskDescription;

   private Optional<UUID> creatorUserId;
   private Optional<UUID> processorUserId;
   private Optional<UUID> responsibleUserId;

   private Optional<UUID> processorGroupId;
   private Optional<UUID> responsibleGroupId;

   private TaskType taskType;
   private TaskCategory taskCategory;
   private Optional<UUID> targetId;

   private TaskState taskState;

   private CMSDate createDate;
   private CMSDate updateDate;
   private CMSDate dueDate;
   private Optional<CMSDate> endDate;

   private String url;
   private String checkUrl;

   private TaskPriority priority;
   private Optional<CMSDate> resubmissionDate;

   private List<TaskMessage> messageList;

}
