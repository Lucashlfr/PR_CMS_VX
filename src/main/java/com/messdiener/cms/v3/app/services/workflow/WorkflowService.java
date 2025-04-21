package com.messdiener.cms.v3.app.services.workflow;

import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.entities.workflows.WorkflowLog;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
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
public class WorkflowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);
    private final DatabaseService databaseService;
    private final WorkflowLogService workflowLogService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_workflow (workflowId VARCHAR(255), tenantId VARCHAR(255), userId VARCHAR(255), workflowType VARCHAR(255), workflowState VARCHAR(255), creationDate LONG, startDate LONG, endDate LONG, lastUpdate LONG, creatorId VARCHAR(255))";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("WorkflowService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize WorkflowService", e);
        }
    }

    public void createWorkflow(Workflow workflow) throws SQLException {
        String sql = "INSERT INTO module_workflow (workflowId, tenantId, userId, workflowType, workflowState, creationDate, startDate, endDate, lastUpdate, creatorId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflow.getWorkflowId().toString());
            preparedStatement.setString(2, workflow.getTenantId().toString());
            preparedStatement.setString(3, workflow.getUserId().toString());
            preparedStatement.setString(4, workflow.getWorkflowType().toString());
            preparedStatement.setString(5, workflow.getWorkflowState().toString());
            preparedStatement.setLong(6, workflow.getCreationDate().toLong());
            preparedStatement.setLong(7, workflow.getStartDate().toLong());
            preparedStatement.setLong(8, workflow.getEndDate().toLong());
            preparedStatement.setLong(9, workflow.getLastUpdateDate().toLong());
            preparedStatement.setString(10, workflow.getCreatorId().toString());
            preparedStatement.executeUpdate();
        }

        for (WorkflowLog log : workflow.getLogs()) {
            workflowLogService.saveWorkflowLog(log);
        }
    }

    public void updateWorkflow(Workflow workflow) throws SQLException {
        String sql = "UPDATE module_workflow SET workflowState = ?, lastUpdate = ? WHERE workflowId = ? AND userId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflow.getWorkflowState().toString());
            preparedStatement.setLong(2, workflow.getLastUpdateDate().toLong());
            preparedStatement.setString(3, workflow.getWorkflowId().toString());
            preparedStatement.setString(4, workflow.getUserId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public Optional<Workflow> getWorkflow(UUID workflowId, UUID personId) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE workflowId = ? AND userId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            preparedStatement.setString(2, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapToWorkflow(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<Workflow> getWorkflow(UUID workflowId) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE workflowId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapToWorkflow(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Workflow> getWorkflows() throws SQLException {
        String sql = "SELECT * FROM module_workflow ORDER BY creationDate DESC";
        List<Workflow> workflows = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                workflows.add(mapToWorkflow(resultSet));
            }
        }
        return workflows;
    }

    public List<Workflow> getWorkflows(UUID workflowId) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE workflowId = ?";
        List<Workflow> workflows = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    workflows.add(mapToWorkflow(resultSet));
                }
            }
        }
        return workflows;
    }

    public Workflow mapToWorkflow(ResultSet resultSet) throws SQLException {
        UUID workflowId = UUID.fromString(resultSet.getString("workflowId"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));
        UUID userId = UUID.fromString(resultSet.getString("userId"));
        WorkflowAttributes.WorkflowType workflowType = WorkflowAttributes.WorkflowType.valueOf(resultSet.getString("workflowType"));
        WorkflowAttributes.WorkflowState workflowState = WorkflowAttributes.WorkflowState.valueOf(resultSet.getString("workflowState"));
        CMSDate creationDate = CMSDate.of(resultSet.getLong("creationDate"));
        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        CMSDate endDate = CMSDate.of(resultSet.getLong("endDate"));
        CMSDate lastUpdateDate = CMSDate.of(resultSet.getLong("lastUpdate"));
        UUID creatorId = UUID.fromString(resultSet.getString("creatorId"));

        List<WorkflowLog> logs = workflowLogService.getWorkflowLogs(workflowId);
        int counter = workflowLogService.getCounter(workflowId);
        int completed = workflowLogService.getCompletedCounter(workflowId);

        return new Workflow(workflowId, tenantId, userId, workflowType, workflowState, creationDate, startDate, endDate, lastUpdateDate, creatorId, logs, counter, completed);
    }
}
