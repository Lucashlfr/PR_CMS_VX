package com.messdiener.cms.v3.app.services.tasks;

import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.shared.enums.tasks.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.tasks.MessageType;
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
public class TaskMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMessageService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String createTasks = "CREATE TABLE IF NOT EXISTS module_tasks (taskId VARCHAR(36), id INT AUTO_INCREMENT PRIMARY KEY, normedTaskName VARCHAR(255), taskTitle TEXT, taskDescription LONGTEXT, creatorUserId VARCHAR(36), processorUserId VARCHAR(36), responsibleUserId VARCHAR(36), processorGroupId VARCHAR(36), responsibleGroupId VARCHAR(36), taskType VARCHAR(255), taskCategory VARCHAR(255), targetId VARCHAR(36), taskState VARCHAR(255), createDate LONG, updateDate LONG, dueDate LONG, endDate LONG, url TEXT, checkUrl TEXT, priority VARCHAR(255), resubmissionDate LONG)";
        String createMessages = "CREATE TABLE IF NOT EXISTS module_tasks_messages (taskId VARCHAR(36), messageId VARCHAR(36), messageType VARCHAR(255), messageTitle TEXT, messageDescription LONGTEXT, messageInformationCascade VARCHAR(2), date LONG, creatorId VARCHAR(36), file BOOLEAN)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement1 = connection.prepareStatement(createTasks);
             PreparedStatement preparedStatement2 = connection.prepareStatement(createMessages)) {
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            LOGGER.info("TaskMessageService initialized and tables ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize TaskMessageService", e);
        }
    }

    public void saveMessage(UUID taskId, TaskMessage taskMessage) throws SQLException {
        String sql = "INSERT INTO module_tasks_messages (taskId, messageId, messageType, messageTitle, messageDescription, messageInformationCascade, date, creatorId, file) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, taskId.toString());
            preparedStatement.setString(2, taskMessage.getMessageId().toString());
            preparedStatement.setString(3, taskMessage.getMessageType().toString());
            preparedStatement.setString(4, taskMessage.getMessageTitle());
            preparedStatement.setString(5, taskMessage.getMessageDescription());
            preparedStatement.setString(6, taskMessage.getMessageInformationCascade().toString());
            preparedStatement.setLong(7, taskMessage.getDate().toLong());
            preparedStatement.setString(8, Utils.optionalId(taskMessage.getCreatorId()));
            preparedStatement.setBoolean(9, taskMessage.isFile());
            preparedStatement.executeUpdate();
            LOGGER.info("Saved message '{}' for task '{}'.", taskMessage.getMessageId(), taskId);
        }
    }

    public List<TaskMessage> getMessagesByTask(UUID taskId) throws SQLException {
        List<TaskMessage> messageList = new ArrayList<>();
        String sql = "SELECT * FROM module_tasks_messages WHERE taskId = ? ORDER BY date";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, taskId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    messageList.add(getTaskMessage(resultSet));
                }
            }
        }
        return messageList;
    }

    public Optional<TaskMessage> getMessageById(UUID messageId) throws SQLException {
        String sql = "SELECT * FROM module_tasks_messages WHERE messageId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, messageId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getTaskMessage(resultSet)) : Optional.empty();
            }
        }
    }

    private TaskMessage getTaskMessage(ResultSet resultSet) throws SQLException {
        UUID messageId = UUID.fromString(resultSet.getString("messageId"));
        MessageType messageType = MessageType.valueOf(resultSet.getString("messageType"));
        String messageTitle = resultSet.getString("messageTitle");
        String messageDescription = resultSet.getString("messageDescription");
        MessageInformationCascade messageInformationCascade = MessageInformationCascade.valueOf(resultSet.getString("messageInformationCascade"));
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        Optional<UUID> creatorId = Utils.getOId(resultSet.getString("creatorId"));
        boolean file = resultSet.getBoolean("file");
        return new TaskMessage(messageId, messageType, messageTitle, messageDescription, messageInformationCascade, date, creatorId, file);
    }
}
