package com.messdiener.cms.v3.app.services.tasks;

import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.tasks.*;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private final DatabaseService databaseService;
    private final TaskMessageService taskMessageService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_tasks (taskId VARCHAR(36), id INT AUTO_INCREMENT PRIMARY KEY, normedTaskName VARCHAR(255), taskTitle TEXT, taskDescription LONGTEXT, creatorUserId VARCHAR(36), processorUserId VARCHAR(36), responsibleUserId VARCHAR(36), processorGroupId VARCHAR(36), responsibleGroupId VARCHAR(36), taskType VARCHAR(255), taskCategory VARCHAR(255), targetId VARCHAR(36), taskState VARCHAR(255), createDate LONG, updateDate LONG, dueDate LONG, endDate LONG, url TEXT, checkUrl TEXT, priority VARCHAR(255), resubmissionDate LONG)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("TaskService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize TaskService", e);
        }
    }

    public void saveTask(Task task) throws SQLException {
        databaseService.delete("module_tasks", "taskId", task.getTaskId().toString());

        String sql = "INSERT INTO module_tasks (taskId, id, normedTaskName, taskTitle, taskDescription, creatorUserId, processorUserId, responsibleUserId, processorGroupId, responsibleGroupId, taskType, taskCategory, targetId, taskState, createDate, updateDate, dueDate, endDate, url, checkUrl, priority, resubmissionDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, task.getTaskId().toString());
            preparedStatement.setInt(2, task.getTaskNumber());
            preparedStatement.setString(3, task.getNormedTaskName());
            preparedStatement.setString(4, task.getTaskTitle());
            preparedStatement.setString(5, task.getTaskDescription());
            preparedStatement.setString(6, Utils.optionalId(task.getCreatorUserId()));
            preparedStatement.setString(7, Utils.optionalId(task.getProcessorUserId()));
            preparedStatement.setString(8, Utils.optionalId(task.getResponsibleUserId()));
            preparedStatement.setString(9, Utils.optionalId(task.getProcessorGroupId()));
            preparedStatement.setString(10, Utils.optionalId(task.getResponsibleGroupId()));
            preparedStatement.setString(11, task.getTaskType().toString());
            preparedStatement.setString(12, task.getTaskCategory().toString());
            preparedStatement.setString(13, Utils.optionalId(task.getTargetId()));
            preparedStatement.setString(14, task.getTaskState().toString());
            preparedStatement.setLong(15, task.getCreateDate().toLong());
            preparedStatement.setLong(16, task.getUpdateDate().toLong());
            preparedStatement.setLong(17, task.getDueDate().toLong());
            preparedStatement.setLong(18, Utils.optionalDate(task.getEndDate()));
            preparedStatement.setString(19, task.getUrl());
            preparedStatement.setString(20, task.getCheckUrl());
            preparedStatement.setString(21, task.getPriority().toString());
            preparedStatement.setLong(22, Utils.optionalDate(task.getResubmissionDate()));
            preparedStatement.executeUpdate();
            LOGGER.info("Task '{}' saved.", task.getTaskId());
        }
    }

    public List<Task> getAllTasks() throws SQLException {
        String sql = "SELECT * FROM module_tasks";
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                tasks.add(getByResultSet(resultSet));
            }
        }
        return tasks;
    }

    public Optional<Task> getTaskById(UUID taskId) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE taskId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, taskId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public Task getByResultSet(ResultSet resultSet) throws SQLException {
        UUID taskId = UUID.fromString(resultSet.getString("taskId"));
        int taskNumber = resultSet.getInt("id");
        String normedTaskName = resultSet.getString("normedTaskName");
        String taskTitle = resultSet.getString("taskTitle");
        String taskDescription = resultSet.getString("taskDescription");
        Optional<UUID> creatorUserId = Utils.getOId(resultSet.getString("creatorUserId"));
        Optional<UUID> processorUserId = Utils.getOId(resultSet.getString("processorUserId"));
        Optional<UUID> responsibleUserId = Utils.getOId(resultSet.getString("responsibleUserId"));
        Optional<UUID> processorGroupId = Utils.getOId(resultSet.getString("processorGroupId"));
        Optional<UUID> responsibleGroupId = Utils.getOId(resultSet.getString("responsibleGroupId"));
        TaskType taskType = TaskType.valueOf(resultSet.getString("taskType"));
        TaskCategory taskCategory = TaskCategory.valueOf(resultSet.getString("taskCategory"));
        Optional<UUID> targetId = Utils.getOId(resultSet.getString("targetId"));
        TaskState taskState = TaskState.valueOf(resultSet.getString("taskState"));
        CMSDate createDate = CMSDate.of(resultSet.getLong("createDate"));
        CMSDate updateDate = CMSDate.of(resultSet.getLong("updateDate"));
        CMSDate dueDate = CMSDate.of(resultSet.getLong("dueDate"));
        Optional<CMSDate> endDate = Utils.getODate(resultSet.getLong("endDate"));
        String url = resultSet.getString("url");
        String checkUrl = resultSet.getString("checkUrl");
        TaskPriority priority = TaskPriority.valueOf(resultSet.getString("priority"));
        Optional<CMSDate> resubmissionDate = Utils.getODate(resultSet.getLong("resubmissionDate"));
        List<TaskMessage> messageList = taskMessageService.getMessagesByTask(taskId);

        return new Task(taskId, taskNumber, normedTaskName, taskTitle, taskDescription, creatorUserId, processorUserId,
                responsibleUserId, processorGroupId, responsibleGroupId, taskType, taskCategory, targetId, taskState,
                createDate, updateDate, dueDate, endDate, url, checkUrl, priority, resubmissionDate, messageList);
    }
}
