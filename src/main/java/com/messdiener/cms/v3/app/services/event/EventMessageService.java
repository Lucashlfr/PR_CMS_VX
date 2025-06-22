package com.messdiener.cms.v3.app.services.event;

import com.messdiener.cms.v3.app.entities.event.data.MessageItem;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.shared.enums.event.EventMessageType;
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
public class EventMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventMessageService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try {
            //language=MariaDB
            createTable("CREATE TABLE IF NOT EXISTS module_event_message (messageId VARCHAR(36), eventId VARCHAR(36), number INT AUTO_INCREMENT PRIMARY KEY, title text, description text, date long, type VARCHAR(30), userId VARCHAR(36))");
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

    public void createMessage(UUID eventId, MessageItem messageItem) throws SQLException {
        databaseService.delete("module_event_message", "messageId", messageItem.getId().toString());

        String sql = "INSERT INTO module_event_message (messageId, eventId, number, title, description, date, type, userId) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, messageItem.getId().toString());
            preparedStatement.setString(2, eventId.toString());
            preparedStatement.setInt(3, messageItem.getNumber());
            preparedStatement.setString(4, messageItem.getTitle());
            preparedStatement.setString(5, messageItem.getDescription());
            preparedStatement.setLong(6, messageItem.getDate().toLong());
            preparedStatement.setString(7, messageItem.getMessageType().toString());
            preparedStatement.setString(8, messageItem.getUserId().toString());
            preparedStatement.executeUpdate();
            LOGGER.info("Updated item '{}' in planer '{}'.", messageItem.getTitle(), eventId);
        }
    }

    public List<MessageItem> getItems(UUID eventId) throws SQLException {
        List<MessageItem> tasks = new ArrayList<>();
        String sql = "SELECT * FROM module_event_message WHERE eventId = ? ORDER BY date";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, eventId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(getTimeline(resultSet));
                }
            }
        }
        return tasks;
    }

    private MessageItem getTimeline(ResultSet resultSet) throws SQLException {

        UUID id = UUID.fromString(resultSet.getString("messageId"));
        int number = resultSet.getInt("number");
        String title = resultSet.getString("title");
        String description =  resultSet.getString("description");
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        EventMessageType type = EventMessageType.valueOf(resultSet.getString("type"));
        UUID userId = UUID.fromString(resultSet.getString("userId"));
        return new MessageItem(id, number, title, description, date, type, userId);
    }
}
