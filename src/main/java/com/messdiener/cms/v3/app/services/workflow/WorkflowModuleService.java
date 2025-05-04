package com.messdiener.cms.v3.app.services.workflow;

import com.messdiener.cms.v3.app.entities.workflow.Workflow;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
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
public class WorkflowModuleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowModuleService.class);
    private final DatabaseService databaseService;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_workflow_modules (moduleId VARCHAR(255) primary key, workflowId VARCHAR(255), ownerId VARCHAR(255), workflowState VARCHAR(255), endDate long, number int, uniqueName VARCHAR(255), metaData LONGTEXT)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("WorkflowController initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize UserConfigurationService", e);
        }
    }


    public Optional<WorkflowModule> getWorkflowModuleById(UUID moduleId) throws SQLException {
        String sql = "SELECT * FROM module_workflow_modules WHERE moduleId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, moduleId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultToWorkflow(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public List<WorkflowModule> getWorkflowModulesByWorkflowId(UUID workflowId) throws SQLException {
        List<WorkflowModule> modules = new ArrayList<>();
        String sql = "SELECT * FROM module_workflow_modules WHERE workflowId = ? order by number";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    modules.add(mapResultToWorkflow(resultSet));
                }
            }
        }
        return modules;
    }

    public void saveWorkflowModule(UUID workflowId, WorkflowModule workflowModule) throws SQLException {
        databaseService.delete("module_workflow_modules", "moduleId", workflowModule.getModuleId().toString());

        String sql = "INSERT INTO module_workflow_modules (moduleId, workflowId, ownerId, workflowState, endDate, number, uniqueName, metaData) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowModule.getModuleId().toString());
            preparedStatement.setString(2, workflowId.toString());
            preparedStatement.setString(3, workflowModule.getOwner() != null ? workflowModule.getOwner().toString() : null);
            preparedStatement.setString(4, workflowModule.getStatus().name());
            preparedStatement.setLong(5, workflowModule.getEndDate() != null ? workflowModule.getEndDate().toLong() : 0L);
            preparedStatement.setInt(6, workflowModule.getNumber());
            preparedStatement.setString(7, workflowModule.getUniqueName().toString());
            preparedStatement.setString(8, workflowModule.getResults());
            preparedStatement.executeUpdate();
        }
    }


    private WorkflowModule mapResultToWorkflow(ResultSet resultSet) throws SQLException {
        UUID moduleId = UUID.fromString(resultSet.getString("moduleId"));
        UUID ownerId = resultSet.getString("ownerId") != null ? UUID.fromString(resultSet.getString("ownerId")) : null;
        String workflowState = resultSet.getString("workflowState");
        WorkflowModuleStatus status = WorkflowModuleStatus.valueOf(workflowState);
        long endDateMillis = resultSet.getLong("endDate");
        CMSDate endDate = new CMSDate(endDateMillis);
        int number = resultSet.getInt("number");
        String metaData = resultSet.getString("metaData");
        WorkflowModuleName uniqueName = WorkflowModuleName.valueOf(resultSet.getString("uniqueName"));

        return WorkflowModule.getWorkflowModule(uniqueName, moduleId, status, number, endDate, metaData, ownerId);
    }


    public Optional<WorkflowModule> getModuleByNumber(UUID workflowId, int currentNumber) throws SQLException {
        String sql = "SELECT * FROM module_workflow_modules WHERE workflowId = ? AND number = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            preparedStatement.setInt(2, currentNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultToWorkflow(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public int getWorkflowModuleCountByWorkflowId(UUID workflowId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM module_workflow_modules WHERE workflowId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateWorkflowModule(WorkflowModule workflowModule) throws SQLException {
        String sql = "UPDATE module_workflow_modules SET ownerId = ?, workflowState = ?, endDate = ?, number = ?, uniqueName = ?, metaData = ? WHERE moduleId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowModule.getOwner() != null ? workflowModule.getOwner().toString() : null);
            preparedStatement.setString(2, workflowModule.getStatus().name());
            preparedStatement.setLong(3, workflowModule.getEndDate() != null ? workflowModule.getEndDate().toLong() : 0L);
            preparedStatement.setInt(4, workflowModule.getNumber());
            preparedStatement.setString(5, workflowModule.getUniqueName().toString());
            preparedStatement.setString(6, workflowModule.getResults());
            preparedStatement.setString(7, workflowModule.getModuleId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public UUID getWorkflowId(UUID moduleId) throws SQLException {
        String sql = "SELECT workflowId FROM module_workflow_modules WHERE moduleId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, moduleId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("workflowId"));
                }
            }
        }
        throw new IndexOutOfBoundsException("Workflow not found.");
    }

    public UUID getCurrentModuleId(UUID workflowID) throws SQLException {
        String sql = "SELECT m.moduleId FROM module_workflow_modules m, module_workflow w WHERE w.workflowId = m.workflowId and m.number = w.currentNumber and w.workflowId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, workflowID.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("m.moduleId"));
                }
            }
        }
        throw new IndexOutOfBoundsException("Workflow not found.");
    }

    public List<WorkflowModule> getActiveModulesForUserByWorkflow(String userId, String workflowId) throws SQLException {
        String sql = "SELECT * FROM module_workflow_modules WHERE ownerId = ? and workflowState = ? or workflowState = ? and workflowId = ? order by number";
        List<WorkflowModule> workflows = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, WorkflowModuleStatus.OPEN.toString());
            preparedStatement.setString(3, WorkflowModuleStatus.IN_PROGRESS.toString());
            preparedStatement.setString(4, workflowId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    WorkflowModule workflowStep = mapResultToWorkflow(resultSet);
                    workflows.add(workflowStep);
                }
            }
        }
        return workflows;
    }

}
