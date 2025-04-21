package com.messdiener.cms.v3.app.services.workflow;

import com.messdiener.cms.v3.app.entities.workflows.WorkflowLog;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowLogService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_workflow_logs (logId VARCHAR(255), workflowId VARCHAR(255), date LONG, creatorId VARCHAR(255), title TEXT, description TEXT)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("WorkflowLogService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize WorkflowLogService", e);
        }
    }

    public void saveWorkflowLog(WorkflowLog workflowLog) throws SQLException {
        String sql = "INSERT INTO module_workflow_logs (logId, workflowId, date, creatorId, title, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowLog.getLogId().toString());
            preparedStatement.setString(2, workflowLog.getWorkflowId().toString());
            preparedStatement.setLong(3, workflowLog.getDate().toLong());
            preparedStatement.setString(4, workflowLog.getCreatorId().toString());
            preparedStatement.setString(5, workflowLog.getTitle());
            preparedStatement.setString(6, workflowLog.getDescription());
            preparedStatement.executeUpdate();
            LOGGER.info("Saved workflow log '{}'.", workflowLog.getLogId());
        }
    }

    public List<WorkflowLog> getWorkflowLogs(UUID workflowId) throws SQLException {
        List<WorkflowLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM module_workflow_logs WHERE workflowId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapToWorkflowLog(resultSet));
                }
            }
        }
        return logs;
    }

    public int getCompletedCounter(UUID id) throws SQLException {
        String sql = "SELECT workflowId, SUM(IF(workflowState = 'COMPLETED', 1, 0)) AS completed_count FROM module_workflow WHERE workflowId = ? GROUP BY workflowId";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("completed_count") : 0;
            }
        }
    }

    public int getCounter(UUID id) throws SQLException {
        String sql = "SELECT workflowId, COUNT(*) AS occurrence_count FROM module_workflow WHERE workflowId = ? GROUP BY workflowId";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("occurrence_count") : 0;
            }
        }
    }

    private WorkflowLog mapToWorkflowLog(ResultSet resultSet) throws SQLException {
        UUID logId = UUID.fromString(resultSet.getString("logId"));
        UUID workflowId = UUID.fromString(resultSet.getString("workflowId"));
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        UUID creatorId = UUID.fromString(resultSet.getString("creatorId"));
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        return new WorkflowLog(logId, workflowId, date, creatorId, title, description);
    }
}
