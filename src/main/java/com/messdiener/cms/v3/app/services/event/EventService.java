package com.messdiener.cms.v3.app.services.event;

import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.event.Event;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.ComponentType;
import com.messdiener.cms.v3.shared.enums.event.EventState;
import com.messdiener.cms.v3.shared.enums.event.EventType;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_events (eventId VARCHAR(255), tenantId VARCHAR(255), updateBy VARCHAR(255), updateDate long, title TEXT, description LONGTEXT, type VARCHAR(255), state VARCHAR(255), startDate LONG, endDate LONG, deadline LONG, schedule TEXT, registrationRelease TEXT, targetGroup VARCHAR(255), location VARCHAR(255), imgUrl TEXT, rinkIndex INT, managerId VARCHAR(255), principals TEXT, expenditure DOUBLE, revenue DOUBLE, pressRelease LONGTEXT, preventionConcept LONGTEXT, notes LONGTEXT, application LONGTEXT)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_events table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }

    }

    private Event getByResultSet(ResultSet resultSet) throws SQLException {
        UUID eventId = UUID.fromString(resultSet.getString("eventId"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));

        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        EventType type = EventType.valueOf(resultSet.getString("type"));
        EventState state = EventState.valueOf(resultSet.getString("state"));

        UUID updatedBy = UUID.fromString(resultSet.getString("updateBy"));
        CMSDate updatedDate = CMSDate.of(resultSet.getLong("updateDate"));

        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        CMSDate endDate = CMSDate.of(resultSet.getLong("endDate"));
        CMSDate deadline = CMSDate.of(resultSet.getLong("deadline"));

        String schedule = resultSet.getString("schedule");
        String registrationRelease = resultSet.getString("registrationRelease");

        String targetGroup = resultSet.getString("targetGroup");
        String location = resultSet.getString("location");
        String imgUrl = resultSet.getString("imgUrl");
        int rinkIndex = resultSet.getInt("rinkIndex");

        UUID managerId = UUID.fromString(resultSet.getString("managerId"));

        List<UUID> principals = new ArrayList<>();
        String principalsRaw = resultSet.getString("principals");
        if (principalsRaw != null && !principalsRaw.isEmpty()) {
            for (String id : principalsRaw.split(",")) {
                principals.add(UUID.fromString(id.trim()));
            }
        }

        double expenditure = resultSet.getDouble("expenditure");
        double revenue = resultSet.getDouble("revenue");

        String pressRelease = resultSet.getString("pressRelease");
        String preventionConcept = resultSet.getString("preventionConcept");
        String notes = resultSet.getString("notes");
        String application = resultSet.getString("application");

        return new Event(eventId, tenantId, updatedBy, updatedDate, title, description, type, state, startDate, endDate, deadline, schedule, registrationRelease, targetGroup, location, imgUrl, rinkIndex, managerId, principals, expenditure, revenue, pressRelease, preventionConcept, notes, application);
    }

    public void save(Event event) throws SQLException {
        databaseService.delete("module_events", "eventId", event.getEventId().toString());

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO module_events (eventId, tenantId, updateBy, updateDate, title, description, type, state, startDate, endDate, deadline, schedule, registrationRelease, targetGroup, location, imgUrl, rinkIndex, managerId, principals, expenditure, revenue, pressRelease, preventionConcept, notes,application) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {

            preparedStatement.setString(1, event.getEventId().toString());
            preparedStatement.setString(2, event.getTenantId().toString());
            preparedStatement.setString(3, event.getUpdatedBy().toString());
            preparedStatement.setLong(4, event.getUpdatedDate().toLong());
            preparedStatement.setString(5, event.getTitle());
            preparedStatement.setString(6, event.getDescription());
            preparedStatement.setString(7, event.getType().toString());
            preparedStatement.setString(8, event.getState().toString());
            preparedStatement.setLong(9, event.getStartDate().toLong());
            preparedStatement.setLong(10, event.getEndDate().toLong());
            preparedStatement.setLong(11, event.getDeadline().toLong());
            preparedStatement.setString(12, event.getSchedule());
            preparedStatement.setString(13, event.getRegistrationRelease());
            preparedStatement.setString(14, event.getTargetGroup());
            preparedStatement.setString(15, event.getLocation());
            preparedStatement.setString(16, event.getImgUrl());
            preparedStatement.setInt(17, event.getRinkIndex());
            preparedStatement.setString(18, event.getManagerId().toString());
            preparedStatement.setString(19, event.getPrincipals().stream().map(UUID::toString).collect(Collectors.joining(",")));
            preparedStatement.setDouble(20, event.getExpenditure());
            preparedStatement.setDouble(21, event.getRevenue());
            preparedStatement.setString(22, event.getPressRelease());
            preparedStatement.setString(23, event.getPreventionConcept());
            preparedStatement.setString(24, event.getNotes());
            preparedStatement.setString(25, event.getApplication());

            preparedStatement.executeUpdate();
            LOGGER.info("Event saved: {}", event.getTitle());
        }
    }

    public List<Event> getEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM module_events";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                events.add(getByResultSet(resultSet));
            }
        }

        return events;
    }

    public Optional<Event> getEventById(UUID eventId) throws SQLException {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM module_events WHERE eventId = ?")) {

            preparedStatement.setString(1, eventId.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(getByResultSet(resultSet));
            }
        }
        return Optional.empty();
    }


    public List<Event> getEventsByTenantId(UUID tenantId) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM module_events where tenantId = ?";

        try (Connection connection = databaseService.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, tenantId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        events.add(getByResultSet(resultSet));
                    }
                }
            }
        }

        return events;
    }

    public List<Event> getEventsAtDeadline() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM module_events WHERE deadline > ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, System.currentTimeMillis());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(getByResultSet(resultSet));
                }
            }
        }

        return events;
    }

}
