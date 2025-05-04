package com.messdiener.cms.v3.app.services.audit;

import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.MessageType;
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
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_audit (log VARCHAR(36), type VARCHAR(255), category VARCHAR(255), connectId VARCHAR(255), userId VARCHAR(36), timestamp long, title text, description text, mic VARCHAR(3), file boolean)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("Configuration table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }
    }

    private AuditLog getLogByResultSet(ResultSet resultSet) throws SQLException {
        UUID logId = UUID.fromString(resultSet.getString("log"));
        MessageType type =  MessageType.valueOf(resultSet.getString("type"));

        ActionCategory category =  ActionCategory.valueOf(resultSet.getString("category"));
        UUID connectedId =  UUID.fromString(resultSet.getString("connectId"));

        UUID userId =  UUID.fromString(resultSet.getString("userId"));
        CMSDate timestamp = CMSDate.of(resultSet.getLong("timestamp"));

        String title =  resultSet.getString("title");
        String description =   resultSet.getString("description");
        MessageInformationCascade mic =   MessageInformationCascade.valueOf(resultSet.getString("mic"));

        boolean file = resultSet.getBoolean("file");
        return new AuditLog(logId, type, category, connectedId, userId, timestamp, title, description, mic, file);
    }

    public List<AuditLog> getLogsByConnectId(UUID connectId) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT * FROM module_audit WHERE connectId = ? order by timestamp DESC";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, connectId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(getLogByResultSet(resultSet));
                }
            }
        }
        return logs;
    }

    public void createLog(AuditLog auditLog) throws SQLException {
        String sql = "INSERT INTO module_audit (log, type, category, connectId, userId, timestamp, title, description, mic, file) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, auditLog.getLogId().toString());
            preparedStatement.setString(2, auditLog.getType().toString());
            preparedStatement.setString(3, auditLog.getCategory().toString());
            preparedStatement.setString(4, auditLog.getConnectedId().toString());
            preparedStatement.setString(5, auditLog.getUserId().toString());
            preparedStatement.setLong(6, auditLog.getTimestamp().toLong());
            preparedStatement.setString(7, auditLog.getTitle());
            preparedStatement.setString(8, auditLog.getDescription());
            preparedStatement.setString(9, auditLog.getMic().toString());
            preparedStatement.setBoolean(10, auditLog.isFile());
            preparedStatement.executeUpdate();
        }
    }

}