package com.messdiener.cms.v3.app.services.planner;

import com.messdiener.cms.v3.app.entities.planer.entities.PlannerLog;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
import com.messdiener.cms.v3.shared.enums.tasks.LogType;
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
public class PlannerLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerLogService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_planer_log (logId VARCHAR(255), planerId VARCHAR(255), tag VARCHAR(255), type VARCHAR(255), date LONG, headline TEXT, log TEXT)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PlannerLogService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerLogService", e);
        }
    }

    public void createLog(UUID planerId, PlannerLog plannerLog) throws SQLException {
        String sql = "INSERT INTO module_planer_log (logId, planerId, tag, type, date, headline, log) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, plannerLog.getLogId().toString());
            preparedStatement.setString(2, planerId.toString());
            preparedStatement.setString(3, plannerLog.getTag().toString());
            preparedStatement.setString(4, plannerLog.getLogType().toString());
            preparedStatement.setLong(5, plannerLog.getDate().toLong());
            preparedStatement.setString(6, plannerLog.getHeadline());
            preparedStatement.setString(7, plannerLog.getLogText());
            preparedStatement.executeUpdate();
            LOGGER.info("Planner log '{}' saved for planer '{}'.", plannerLog.getLogId(), planerId);
        }
    }

    public List<PlannerLog> getLogs(UUID planerId) throws SQLException {
        return getLogsByQuery("SELECT * FROM module_planer_log WHERE planerId = ? ORDER BY date DESC", stmt -> {
            stmt.setString(1, planerId.toString());
        });
    }

    public List<PlannerLog> getLogs(UUID planerId, PlanerTag tag) throws SQLException {
        return getLogsByQuery("SELECT * FROM module_planer_log WHERE planerId = ? AND tag = ? ORDER BY date DESC", stmt -> {
            stmt.setString(1, planerId.toString());
            stmt.setString(2, tag.toString());
        });
    }

    public List<PlannerLog> getLogs(UUID planerId, PlanerTag tag, LogType logType) throws SQLException {
        return getLogsByQuery("SELECT * FROM module_planer_log WHERE planerId = ? AND tag = ? AND type = ? ORDER BY date DESC LIMIT 5", stmt -> {
            stmt.setString(1, planerId.toString());
            stmt.setString(2, tag.toString());
            stmt.setString(3, logType.toString());
        });
    }

    private List<PlannerLog> getLogsByQuery(String sql, SQLConsumer<PreparedStatement> preparer) throws SQLException {
        List<PlannerLog> logs = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparer.accept(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(getPlannerLog(resultSet));
                }
            }
        }
        return logs;
    }

    private PlannerLog getPlannerLog(ResultSet resultSet) throws SQLException {
        UUID logId = UUID.fromString(resultSet.getString("logId"));
        PlanerTag tag = PlanerTag.valueOf(resultSet.getString("tag"));
        LogType type = LogType.valueOf(resultSet.getString("type"));
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        String headline = resultSet.getString("headline");
        String logText = resultSet.getString("log");
        return new PlannerLog(logId, tag, type, date, headline, logText);
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
