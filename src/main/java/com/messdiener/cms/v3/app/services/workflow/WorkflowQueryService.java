package com.messdiener.cms.v3.app.services.workflow;

import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
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
public class WorkflowQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowQueryService.class);
    private final DatabaseService databaseService;
    private final WorkflowService workflowService;

    @PostConstruct
    public void init() {
        LOGGER.info("WorkflowQueryService initialized.");
    }

    public List<Workflow> getWorkflowsByUser(UUID userId, WorkflowAttributes.WorkflowState workflowState) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE userId = ? AND workflowState = ?";
        return executeWorkflowQuery(sql, preparedStatement -> {
            preparedStatement.setString(1, userId.toString());
            preparedStatement.setString(2, workflowState.toString());
        });
    }

    public List<Workflow> getWorkflowsByUser(UUID userId) throws SQLException {
        String sql = "SELECT * FROM module_workflow WHERE userId = ?";
        return executeWorkflowQuery(sql, preparedStatement -> preparedStatement.setString(1, userId.toString()));
    }

    public List<Workflow> getWorkflowsSummary() throws SQLException {
        String sql = "SELECT * FROM module_workflow GROUP BY workflowId";
        List<Workflow> workflows = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Workflow workflow = workflowService.mapToWorkflow(resultSet);
                if (workflow.getCompletedCounter() == workflow.getCounter()) continue;
                workflows.add(workflow);
            }
        }
        return workflows;
    }

    private List<Workflow> executeWorkflowQuery(String sql, SQLConsumer<PreparedStatement> consumer) throws SQLException {
        List<Workflow> workflows = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            consumer.accept(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    workflows.add(workflowService.mapToWorkflow(resultSet));
                }
            }
        }
        return workflows;
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
