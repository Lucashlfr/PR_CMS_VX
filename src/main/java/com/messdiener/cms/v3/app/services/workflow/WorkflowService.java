package com.messdiener.cms.v3.app.services.workflow;

import com.messdiener.cms.v3.app.entities.workflow.Workflow;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowType;
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

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_workflow (workflowId VARCHAR(255) primary key , ownerId VARCHAR(255), workflowType VARCHAR(255), workflowState VARCHAR(255), creationDate long, endDate long, currentNumber int, metaData LONGTEXT)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("WorkflowController initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize UserConfigurationService", e);
        }
    }


    public Optional<Workflow> getWorkflowById(UUID workflowId) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE workflowId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultToWorkflow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Workflow> getWorkflowsByUserId(UUID ownerId) throws SQLException {
        List<Workflow> workflows = new ArrayList<>();
        String sql = "SELECT * FROM module_workflow WHERE ownerId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, ownerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    workflows.add(mapResultToWorkflow(resultSet));
                }
            }
        }
        return workflows;
    }

    public List<Workflow> getAllWorkflowsByTenant(UUID tenantId) throws SQLException {
        List<Workflow> workflows = new ArrayList<>();
        String sql = "SELECT * FROM module_workflow, module_person WHERE module_person.person_id = ownerId AND module_person.tenant_id = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    workflows.add(mapResultToWorkflow(resultSet));
                }
            }
        }
        return workflows;
    }

    public void saveWorkflow(Workflow workflow) throws SQLException {
        String sql = "INSERT INTO module_workflow (workflowId, ownerId, workflowType, workflowState, creationDate, endDate, currentNumber, metaData) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE ownerId = VALUES(ownerId), workflowType = VALUES(workflowType), workflowState = VALUES(workflowState), creationDate = VALUES(creationDate), endDate = VALUES(endDate), currentNumber = VALUES(currentNumber), metaData = VALUES(metaData)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflow.getWorkflowId().toString());
            preparedStatement.setString(2, workflow.getOwnerId().toString());
            preparedStatement.setString(3, workflow.getWorkflowType().name());
            preparedStatement.setString(4, workflow.getCMSState().name());
            preparedStatement.setLong(5, workflow.getCreationDate().toLong());
            preparedStatement.setLong(6, workflow.getEndDate() != null ? workflow.getEndDate().toLong() : 0L);
            preparedStatement.setInt(7, workflow.getCurrentNumber());
            preparedStatement.setString(8, workflow.getMetadata());
            preparedStatement.executeUpdate();
            LOGGER.info("Saved workflow with ID {}", workflow.getWorkflowId());
        }
    }

    private Workflow mapResultToWorkflow(ResultSet resultSet) throws SQLException {
        return new Workflow(
                UUID.fromString(resultSet.getString("workflowId")),
                UUID.fromString(resultSet.getString("ownerId")),
                WorkflowType.valueOf(resultSet.getString("workflowType")),
                CMSState.valueOf(resultSet.getString("workflowState")),
                new CMSDate(resultSet.getLong("creationDate")),
                new CMSDate(resultSet.getLong("endDate")),
                resultSet.getInt("currentNumber"),
                resultSet.getString("metaData")
        );
    }


    public List<Workflow> getRelevantWorkflows(String userId) {

        String sql = "SELECT * FROM module_workflow, module_workflow_modules WHERE module_workflow_modules.ownerId = ? and module_workflow.workflowState = ? and module_workflow_modules.workflowId = module_workflow.workflowId and module_workflow.currentNumber = module_workflow_modules.number";
        List<Workflow> workflows = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, CMSState.ACTIVE.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Workflow workflow = mapResultToWorkflow(resultSet);
                    workflows.add(workflow);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return workflows;
    }

    public Map<String, Integer> countWorkflowStates(UUID userId) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE ownerId = ?";
        Map<String, Integer> stateCountMap = new HashMap<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    CMSState state = CMSState.valueOf(resultSet.getString("workflowState"));
                    stateCountMap.put(state.toString(), stateCountMap.getOrDefault(state.toString(), 0) + 1);
                }
            }
        }
        return stateCountMap;
    }

    public Map<String, Integer> countWorkflowStatesByTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT workflowState FROM module_workflow, module_person WHERE module_person.person_id = ownerId AND module_person.tenant_id = ?";
        Map<String, Integer> stateCountMap = new HashMap<>();

        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    CMSState state = CMSState.valueOf(resultSet.getString("module_workflow.workflowState"));
                    stateCountMap.put(state.toString(), stateCountMap.getOrDefault(state.toString(), 0) + 1);
                }
            }
        }
        return stateCountMap;
    }


}
