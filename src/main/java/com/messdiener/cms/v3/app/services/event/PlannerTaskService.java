package com.messdiener.cms.v3.app.services.event;

import com.messdiener.cms.v3.app.entities.event.PlanerTask;
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
public class PlannerTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerTaskService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try {
            createTable("CREATE TABLE IF NOT EXISTS module_planer_principal (planerId VARCHAR(255), userId VARCHAR(255))");
            createTable("CREATE TABLE IF NOT EXISTS module_planer_map (planerId VARCHAR(255), userId VARCHAR(255), date LONG)");
            createTable("CREATE TABLE IF NOT EXISTS module_planer_tasks (taskId VARCHAR(255), planerId VARCHAR(255), id INT AUTO_INCREMENT PRIMARY KEY, taskName TEXT, taskDescription TEXT, taskState VARCHAR(255), created LONG, updated LONG, lable VARCHAR(255))");
            LOGGER.info("PlannerTaskService initialized and tables ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerTaskService", e);
        }
    }

    private void createTable(String sql) throws SQLException {
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    public void updateTask(UUID eventId, PlanerTask planerTask) throws SQLException {
        databaseService.delete("module_planer_tasks", "taskId", planerTask.getTaskId().toString());

        String sql = "INSERT INTO module_planer_tasks (taskId, planerId, id, taskName, taskDescription, taskState, created, updated, lable) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerTask.getTaskId().toString());
            preparedStatement.setString(2, eventId.toString());
            preparedStatement.setInt(3, planerTask.getTaskNumber());
            preparedStatement.setString(4, planerTask.getTaskName());
            preparedStatement.setString(5, planerTask.getTaskDescription());
            preparedStatement.setString(6, planerTask.getState().toString());
            preparedStatement.setLong(7, planerTask.getCreated().toLong());
            preparedStatement.setLong(8, planerTask.getUpdated().toLong());
            preparedStatement.setString(9, planerTask.getLable());
            preparedStatement.executeUpdate();
            LOGGER.info("Updated task '{}' in planer '{}'.", planerTask.getTaskId(), eventId);
        }
    }

    public List<PlanerTask> getTasks(UUID planerId) throws SQLException {
        List<PlanerTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM module_planer_tasks WHERE planerId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(getPlanerTask(resultSet));
                }
            }
        }
        return tasks;
    }

    public Optional<PlanerTask> getTaskById(UUID taskId) throws SQLException {
        String sql = "SELECT * FROM module_planer_tasks WHERE taskId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, taskId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getPlanerTask(resultSet)) : Optional.empty();
            }
        }
    }

    public double progress(UUID planerId) throws SQLException {
        String sql = "SELECT * FROM module_planer_tasks WHERE planerId = ?";
        double counter = 0;
        double completedTasks = 0;

        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    PlanerTask planerTask = getPlanerTask(resultSet);
                    counter++;
                    if (planerTask.getState() == CMSState.COMPLETED) completedTasks++;
                }
            }
        }

        return counter == 0 ? 0 : (completedTasks / counter) * 100;
    }

    private PlanerTask getPlanerTask(ResultSet resultSet) throws SQLException {
        UUID taskId = UUID.fromString(resultSet.getString("taskId"));
        int taskNumber = resultSet.getInt("id");
        String taskName = resultSet.getString("taskName");
        String taskDescription = resultSet.getString("taskDescription");
        CMSState taskState = CMSState.valueOf(resultSet.getString("taskState"));
        CMSDate created = CMSDate.of(resultSet.getLong("created"));
        CMSDate updated = CMSDate.of(resultSet.getLong("updated"));
        String lable = resultSet.getString("lable");

        return new PlanerTask(taskId, taskNumber, taskName, taskDescription, taskState, created, updated, lable);
    }
}
