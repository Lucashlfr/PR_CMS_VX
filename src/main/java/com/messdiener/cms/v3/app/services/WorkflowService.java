package com.messdiener.cms.v3.app.services;

import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.entities.workflows.WorkflowLog;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkflowService {

    private DatabaseService databaseService;

    public WorkflowService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        try {
            this.databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_workflow (workflowId VARCHAR(255), tenantId VARCHAR(255), userId VARCHAR(255), workflowType VARCHAR(255), workflowState VARCHAR(255), creationDate long, startDate long, endDate long, lastUpdate long, creatorId VARCHAR(255))").executeUpdate();
            this.databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_workflow_logs (logId VARCHAR(255), workflowId VARCHAR(255), date long, creatorId VARCHAR(255), title TEXT, description TEXT)").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void createWorkflow(Workflow workflow) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_workflow (workflowId, tenantId, userId, workflowType, workflowState, creationDate, startDate, endDate, lastUpdate, creatorId) VALUES (?,?,?,?,?,?,?,?,?,?)");
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

        for(WorkflowLog log : workflow.getLogs()) {
            saveWorkflowLog(log);
        }
    }

    public void saveWorkflowLog(WorkflowLog workflowLog) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_workflow_logs (logId, workflowId, date, creatorId, title, description) VALUES (?,?,?,?,?,?)");
        preparedStatement.setString(1, workflowLog.getLogId().toString());
        preparedStatement.setString(2, workflowLog.getWorkflowId().toString());
        preparedStatement.setLong(3, workflowLog.getDate().toLong());
        preparedStatement.setString(4, workflowLog.getCreatorId().toString());
        preparedStatement.setString(5, workflowLog.getTitle());
        preparedStatement.setString(6, workflowLog.getDescription());
        preparedStatement.executeUpdate();
    }

    private Workflow getWorkflow(ResultSet resultSet) throws SQLException {
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

        List<WorkflowLog> logs = getWorkflowLogs(workflowId);
        return new Workflow(workflowId, tenantId, userId, workflowType, workflowState, creationDate, startDate, endDate, lastUpdateDate, creatorId, logs);
    }

    public List<WorkflowLog> getWorkflowLogs(UUID workflowId) throws SQLException {
        return new ArrayList<>();
    }

    public List<Workflow> getWorkflowsByUser(UUID userId, WorkflowAttributes.WorkflowState workflowState) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_workflow WHERE userId = ? AND workflowState = ?");
        preparedStatement.setString(1, userId.toString());
        preparedStatement.setString(2, workflowState.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Workflow> workflows = new ArrayList<>();
        while(resultSet.next()) {
            workflows.add(getWorkflow(resultSet));
        }
        return workflows;
    }

    public Optional<Workflow> getWorkflow(UUID workflowId, UUID personId) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_workflow WHERE workflowId = ? AND userId = ?");
        preparedStatement.setString(1, workflowId.toString());
        preparedStatement.setString(2, personId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            return Optional.of(getWorkflow(resultSet));
        }
        return Optional.empty();
    }

    public void updateWorkflow(Workflow workflow) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("UPDATE module_workflow SET workflowState = ?, lastUpdate = ? WHERE workflowId = ? AND userId = ?");
        preparedStatement.setString(1, workflow.getWorkflowState().toString());
        preparedStatement.setLong(2, workflow.getLastUpdateDate().toLong());
        preparedStatement.setString(3, workflow.getWorkflowId().toString());
        preparedStatement.setString(4, workflow.getUserId().toString());
        preparedStatement.executeUpdate();
    }

    public List<Workflow> getWorkflowsByUser(UUID userId) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_workflow WHERE userId = ?");
        preparedStatement.setString(1, userId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Workflow> workflows = new ArrayList<>();
        while(resultSet.next()) {
            workflows.add(getWorkflow(resultSet));
        }
        return workflows;
    }
}
