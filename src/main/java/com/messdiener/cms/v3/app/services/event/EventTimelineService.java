package com.messdiener.cms.v3.app.services.event;

import com.messdiener.cms.v3.app.entities.event.data.TimelineItem;
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
public class EventTimelineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventTimelineService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try {
            createTable("CREATE TABLE IF NOT EXISTS module_event_timeline (timelineId VARCHAR(36), eventId VARCHAR(36), title text, description text, date long)");
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

    public void createTimeline(UUID eventId, TimelineItem timelineItem) throws SQLException {
        databaseService.delete("module_event_timeline", "timelineId", timelineItem.getId().toString());

        String sql = "INSERT INTO module_event_timeline (timelineId, eventId, title, description, date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, timelineItem.getId().toString());
            preparedStatement.setString(2, eventId.toString());
            preparedStatement.setString(3, timelineItem.getTitle());
            preparedStatement.setString(4, timelineItem.getDescription());
            preparedStatement.setLong(5, timelineItem.getDate().toLong());
            preparedStatement.executeUpdate();
            LOGGER.info("Updated item '{}' in planer '{}'.", timelineItem.getTitle(), eventId);
        }
    }

    public List<TimelineItem> getItems(UUID eventId) throws SQLException {
        List<TimelineItem> tasks = new ArrayList<>();
        String sql = "SELECT * FROM module_event_timeline WHERE eventId = ? ORDER BY date";
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

    private TimelineItem getTimeline(ResultSet resultSet) throws SQLException {

        UUID id = UUID.fromString(resultSet.getString("timelineId"));
        String title = resultSet.getString("title");
        String description =  resultSet.getString("description");
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        return new TimelineItem(id, title, description, date);
    }
}
