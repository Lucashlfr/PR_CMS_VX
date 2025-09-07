package com.messdiener.cms.v3.app.services.event;

import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.event.Event;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.ComponentType;
import com.messdiener.cms.v3.shared.enums.event.EventState;
import com.messdiener.cms.v3.shared.enums.event.EventType;
import com.messdiener.cms.v3.shared.enums.tenant.Tenant;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.info.ProcessInfoContributor;
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
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_events (eventId VARCHAR(36), tenant VARCHAR(4), number int, title TEXT, description TEXT, eventType VARCHAR(50), eventState VARCHAR(50), startDate long, endDate long, deadline long, creationDate long, resubmission long, lastUpdate long, schedule LONGTEXT, registrationRelease LONGTEXT, targetGroup TEXT, location TEXT, imgUrl TEXT, riskIndex int, currentEditor VARCHAR(36), createdBy VARCHAR(36), updatedBy VARCHAR(36), principal VARCHAR(36), manager VARCHAR(36), expenditure double, revenue double, pressRelease LONGTEXT, preventionConcept LONGTEXT, notes LONGTEXT, application LONGTEXT)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_events table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }

    }

    private Event getByResultSet(ResultSet resultSet) throws SQLException {

        // Identifikation
        UUID eventId = UUID.fromString(resultSet.getString("eventId"));
        Tenant tenant = Tenant.valueOf(resultSet.getString("tenant"));

        int number = resultSet.getInt("number");

        // Allgemeine Informationen
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        EventType type = EventType.valueOf(resultSet.getString("eventType"));
        EventState state = EventState.valueOf(resultSet.getString("eventState"));

        // Zeitliche Angaben
        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        CMSDate endDate = CMSDate.of(resultSet.getLong("endDate"));
        CMSDate deadline = CMSDate.of(resultSet.getLong("deadline"));

        CMSDate creationDate = CMSDate.of(resultSet.getLong("creationDate"));
        CMSDate resubmission = CMSDate.of(resultSet.getLong("resubmission"));
        CMSDate lastUpdate = CMSDate.of(resultSet.getLong("lastUpdate"));

        String schedule = resultSet.getString("schedule");
        String registrationRelease = resultSet.getString("registrationRelease");

        // Zielgruppe & Ort
        String targetGroup = resultSet.getString("targetGroup");
        String location = resultSet.getString("location");
        String imgUrl = resultSet.getString("imgUrl");
        int riskIndex = resultSet.getInt("riskIndex");

        // Organisation & Verantwortliche
        UUID currentEditor = UUID.fromString(resultSet.getString("currentEditor"));
        UUID createdBy = UUID.fromString(resultSet.getString("createdBy"));
        UUID principal = UUID.fromString(resultSet.getString("principal"));
        UUID manager = UUID.fromString(resultSet.getString("manager"));

        // Finanzen
        double expenditure = resultSet.getDouble("expenditure");
        double revenue = resultSet.getDouble("revenue");

        // Presse & Dokumentation
        String pressRelease = resultSet.getString("pressRelease");
        String preventionConcept = resultSet.getString("preventionConcept");
        String notes = resultSet.getString("notes");
        String application = resultSet.getString("application");

        return new Event(eventId, tenant, number, title, description, type, state, startDate, endDate, deadline, creationDate, resubmission, lastUpdate, schedule, registrationRelease, targetGroup, location, imgUrl, riskIndex,
                currentEditor, createdBy, principal, manager, expenditure, revenue, pressRelease, preventionConcept, notes, application);
    }

    public void save(Event event) throws SQLException {
        databaseService.delete("module_events", "eventId", event.getEventId().toString());

        String sql = "INSERT INTO module_events (eventId, tenant, number, title, description, eventType, eventState, startDate, endDate, deadline, creationDate, resubmission, lastUpdate, schedule, registrationRelease, targetGroup, location, imgUrl, riskIndex, currentEditor, createdBy, updatedBy, principal, manager, expenditure, revenue, pressRelease, preventionConcept, notes, application) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setString(1, event.getEventId().toString());
            preparedStatement.setString(2, event.getTenant().toString());
            preparedStatement.setInt(3, event.getNumber());
            preparedStatement.setString(4, event.getTitle());
            preparedStatement.setString(5, event.getDescription());
            preparedStatement.setString(6, event.getType().toString());
            preparedStatement.setString(7, event.getState().toString());
            preparedStatement.setLong(8, event.getStartDate().toLong());
            preparedStatement.setLong(9, event.getEndDate().toLong());
            preparedStatement.setLong(10, event.getDeadline().toLong());
            preparedStatement.setLong(11, event.getCreationDate().toLong());
            preparedStatement.setLong(12, event.getResubmission().toLong());
            preparedStatement.setLong(13, event.getLastUpdate().toLong());
            preparedStatement.setString(14, event.getSchedule());
            preparedStatement.setString(15, event.getRegistrationRelease());
            preparedStatement.setString(16, event.getTargetGroup());
            preparedStatement.setString(17, event.getLocation());
            preparedStatement.setString(18, event.getImgUrl());
            preparedStatement.setInt(19, event.getRiskIndex());
            preparedStatement.setString(20, event.getCurrentEditor().toString());
            preparedStatement.setString(21, event.getCreatedBy().toString());
            preparedStatement.setString(22, event.getCurrentEditor().toString());
            preparedStatement.setString(23, event.getPrincipal().toString());
            preparedStatement.setString(24, event.getManager().toString());
            preparedStatement.setDouble(25, event.getExpenditure());
            preparedStatement.setDouble(26, event.getRevenue());
            preparedStatement.setString(27, event.getPressRelease());
            preparedStatement.setString(28, event.getPreventionConcept());
            preparedStatement.setString(29, event.getNotes());
            preparedStatement.setString(30, event.getApplication());

            preparedStatement.executeUpdate();
            LOGGER.info("Event saved: {}", event.getTitle());
        }
    }

    public List<Event> getEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM module_events ORDER BY endDate DESC";

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


    public List<Event> getEventsByTenant(Tenant tenant) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM module_events where tenant = ?";

        try (Connection connection = databaseService.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, tenant.toString());
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

    public List<Event> getEventsForState() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM module_events WHERE eventState = ? or eventState = ? ORDER BY endDate DESC";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, EventState.CONFIRMED.toString());
            preparedStatement.setString(2, EventState.COMPLETED.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(getByResultSet(resultSet));
                }
            }
        }

        return events;
    }

}
