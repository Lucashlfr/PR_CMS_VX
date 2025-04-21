package com.messdiener.cms.v3.app.services.tasks;

import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.tasks.TaskState;
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
public class TaskQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskQueryService.class);
    private final DatabaseService databaseService;
    private final TaskService taskService;

    @PostConstruct
    public void init() {
        LOGGER.info("TaskQueryService initialized.");
    }

    public List<Task> getTaskByUserId(UUID id) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE processorUserId = ? ORDER BY createDate DESC";
        return executeTaskQuery(sql, ps -> ps.setString(1, id.toString()));
    }

    public List<Task> getOpenTasksByUser(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE processorUserId = ? AND taskState != ?";
        return executeTaskQuery(sql, ps -> {
            ps.setString(1, uuid.toString());
            ps.setString(2, TaskState.COMPLETED.toString());
        });
    }

    public List<Task> getMappedTaskByUserId(UUID id) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE (targetId = ? OR processorUserId = ?) ORDER BY createDate DESC";
        return executeTaskQuery(sql, ps -> {
            ps.setString(1, id.toString());
            ps.setString(2, id.toString());
        });
    }

    public boolean normedTaskExists(UUID personId, String normedName) throws SQLException {
        String sql = "SELECT 1 FROM module_tasks WHERE targetId = ? AND normedTaskName = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, normedName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Optional<Task> getNormedTaskById(UUID userId, String name) throws SQLException {
        String sql = "SELECT * FROM module_tasks WHERE targetId = ? AND normedTaskName = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId.toString());
            preparedStatement.setString(2, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(taskService.getByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) AS total_entries FROM module_tasks";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("total_entries");
            }
        }
        return 0;
    }

    private List<Task> executeTaskQuery(String sql, SQLConsumer<PreparedStatement> preparer) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparer.accept(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(taskService.getByResultSet(resultSet));
                }
            }
        }
        return tasks;
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}