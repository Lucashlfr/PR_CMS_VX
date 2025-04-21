package com.messdiener.cms.v3.app.services.planner;

import com.messdiener.cms.v3.app.entities.planer.entities.PlanerSubEvent;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlannerSubEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerSubEventService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_planer_sub_event (subId VARCHAR(255), planerId VARCHAR(255), lastModified LONG, personId VARCHAR(255), title TEXT, html LONGTEXT)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PlannerSubEventService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerSubEventService", e);
        }
    }

    public void saveSubEvent(UUID planerId, PlanerSubEvent planerSubEvent) throws SQLException {
        databaseService.delete("module_planer_sub_event", "subId", planerSubEvent.getSubId().toString());
        String sql = "INSERT INTO module_planer_sub_event (subId, planerId, lastModified, personId, title, html) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerSubEvent.getSubId().toString());
            preparedStatement.setString(2, planerId.toString());
            preparedStatement.setLong(3, planerSubEvent.getLastModified().toLong());
            preparedStatement.setString(4, planerSubEvent.getPersonId().toString());
            preparedStatement.setString(5, planerSubEvent.getTitle());
            preparedStatement.setString(6, planerSubEvent.getHtml());
            preparedStatement.executeUpdate();
            LOGGER.info("Sub event '{}' saved for planner '{}'.", planerSubEvent.getSubId(), planerId);
        }
    }

    public List<PlanerSubEvent> loadSubEvents(UUID planerId) throws SQLException {
        List<PlanerSubEvent> subEvents = new ArrayList<>();
        String sql = "SELECT * FROM module_planer_sub_event WHERE planerId = ? ORDER BY title DESC";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    subEvents.add(getPlanerSubEvent(resultSet));
                }
            }
        }
        return subEvents;
    }

    public Optional<PlanerSubEvent> getSubEvent(UUID subId) throws SQLException {
        String sql = "SELECT * FROM module_planer_sub_event WHERE subId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, subId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(getPlanerSubEvent(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    private PlanerSubEvent getPlanerSubEvent(ResultSet resultSet) throws SQLException {
        UUID subId = UUID.fromString(resultSet.getString("subId"));
        CMSDate lastModified = CMSDate.of(resultSet.getLong("lastModified"));
        UUID personId = UUID.fromString(resultSet.getString("personId"));
        String title = resultSet.getString("title");
        String html = resultSet.getString("html");
        return new PlanerSubEvent(subId, lastModified, personId, title, html);
    }
}