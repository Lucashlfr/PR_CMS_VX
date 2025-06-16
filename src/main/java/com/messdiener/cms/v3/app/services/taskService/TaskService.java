package com.messdiener.cms.v3.app.services.taskService;

import com.messdiener.cms.v3.app.entities.task.Task;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_tasks (taskId VARCHAR(36),  number INT AUTO_INCREMENT PRIMARY KEY, link VARCHAR(36), creator VARCHAR(36),editor VARCHAR(36), lastUpdate long, creationDate long, endDate long, title text, description text, taskState VARCHAR(50), priority VARCHAR(10))")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_tasks table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_tasks table", e);
            throw new RuntimeException(e);
        }

    }

    private Task getTaskFromResultSet(ResultSet resultSet) throws SQLException {
        UUID taskId = UUID.fromString(resultSet.getString("taskId"));
        int number =  resultSet.getInt("number");
        UUID link = UUID.fromString(resultSet.getString("link"));

        UUID creator =  UUID.fromString(resultSet.getString("creator"));
        UUID editor =   UUID.fromString(resultSet.getString("editor"));

        CMSDate lastUpdate = CMSDate.of(resultSet.getLong("lastUpdate"));
        CMSDate creationDate =  CMSDate.of(resultSet.getLong("creationDate"));
        CMSDate endDate =   CMSDate.of(resultSet.getLong("endDate"));

        String title =  resultSet.getString("title");
        String description =   resultSet.getString("description");

        CMSState taskState =  CMSState.valueOf(resultSet.getString("taskState"));
        String priority =   resultSet.getString("priority");
        return new Task(taskId, number, link, creator, editor, lastUpdate, creationDate, endDate, title, description, taskState, priority);
    }

    public void saveTask(Task task) throws SQLException {
        databaseService.delete("module_tasks", "taskId", task.getTaskId().toString());

        String sql = "INSERT INTO module_tasks (taskId, number, link, creator, editor, lastUpdate, creationDate, endDate, title, description, taskState, priority) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, task.getTaskId().toString());
            preparedStatement.setInt(2, task.getNumber());
            preparedStatement.setString(3, task.getLink().toString());
            preparedStatement.setString(4, task.getCreator().toString());
            preparedStatement.setString(5, task.getEditor().toString());
            preparedStatement.setLong(6, System.currentTimeMillis());
            preparedStatement.setLong(7, task.getCreationDate().toLong());
            preparedStatement.setLong(8, task.getEndDate().toLong());
            preparedStatement.setString(9, task.getTitle());
            preparedStatement.setString(10, task.getDescription());
            preparedStatement.setString(11, task.getTaskState().toString());
            preparedStatement.setString(12, task.getPriority());
            preparedStatement.executeUpdate();
        }
    }

    public List<Task> getTasksByLink(UUID link) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE link = ?";
        List<Task> tasks = new ArrayList<>();
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, link.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    tasks.add(getTaskFromResultSet(resultSet));
                }
            }
        }
        return tasks;
    }

    public Optional<Task> getTaskById(UUID id) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE taskId = ?";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next() ? Optional.of(getTaskFromResultSet(resultSet)) : Optional.empty();
            }
        }
    }
}
