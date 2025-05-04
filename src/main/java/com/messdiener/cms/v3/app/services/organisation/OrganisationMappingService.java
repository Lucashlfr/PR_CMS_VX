package com.messdiener.cms.v3.app.services.organisation;

import com.messdiener.cms.v3.app.entities.organisation.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.utils.other.Pair;
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
public class OrganisationMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationMappingService.class);
    private final DatabaseService databaseService;
    private final PersonService personService;
    private final OrganisationEventService organisationEventService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_organisation_map (eventId VARCHAR(255), userId VARCHAR(255), response INT, schedule INT, activity INT)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("OrganisationMappingService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Error initializing OrganisationMappingService", e);
        }
    }

    public void setMapState(UUID eventId, UUID userId, int response, int schedule, int activity) throws SQLException {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM module_organisation_map WHERE eventId = ? AND userId = ?")) {
            deleteStatement.setString(1, eventId.toString());
            deleteStatement.setString(2, userId.toString());
            deleteStatement.executeUpdate();
        }

        try (Connection connection = databaseService.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO module_organisation_map (eventId, userId, response, schedule, activity) VALUES (?, ?, ?, ?, ?)")) {
            insertStatement.setString(1, eventId.toString());
            insertStatement.setString(2, userId.toString());
            insertStatement.setInt(3, response);
            insertStatement.setInt(4, schedule);
            insertStatement.setInt(5, activity);
            insertStatement.executeUpdate();
        }
    }

    public boolean isRegistered(UUID personId, UUID eventId) throws SQLException {
        String sql = "SELECT * FROM module_organisation_map WHERE userId = ? AND eventId = ? AND response = 1 AND schedule = 0";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, eventId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean isScheduled(UUID personId, UUID eventId) throws SQLException {
        String sql = "SELECT * FROM module_organisation_map WHERE userId = ? AND eventId = ? AND response = 1 AND schedule = 1";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, eventId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Person> getScheduledPersons(UUID eventId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_map map, module_person person WHERE map.userId = person_id AND map.eventId = ? AND response = 1 AND schedule = 1";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, eventId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(personService.getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getRegisteredPersons(UUID eventId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_map map, module_person person WHERE map.userId = person_id AND map.eventId = ? AND response = 1 AND schedule = 0";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, eventId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(personService.getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<OrganisationEvent> getEventsByPerson(UUID personId, OrganisationType organisationType, int response, int schedule) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events, module_organisation_map WHERE module_organisation_events.id = module_organisation_map.eventId AND userId = ? AND type = ? AND response = ? AND schedule = ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, organisationType.toString());
            preparedStatement.setInt(3, response);
            preparedStatement.setInt(4, schedule);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(organisationEventService.getByResultSet(resultSet));
                }
            }
        }
        return list;
    }

    public List<Pair<OrganisationEvent, Integer>> getAllEventsByPerson(UUID personId, OrganisationType organisationType) throws SQLException {
        List<Pair<OrganisationEvent, Integer>> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events, module_organisation_map WHERE module_organisation_events.id = module_organisation_map.eventId AND userId = ? AND type = ? AND startDate > ? ORDER BY startDate ASC";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, organisationType.toString());
            preparedStatement.setLong(3, System.currentTimeMillis());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    OrganisationEvent event = organisationEventService.getByResultSet(resultSet);
                    int schedule = resultSet.getInt("schedule");
                    int response = resultSet.getInt("response");
                    int state = (schedule == 1 && response == 1) ? 2 : (schedule == 0 && response == 1) ? 1 : 0;
                    list.add(new Pair<>(event, state));
                }
            }
        }
        return list;
    }

    public List<OrganisationEvent> getNextEventsByPerson(UUID personId, OrganisationType organisationType, int response, int schedule) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events, module_organisation_map WHERE module_organisation_events.id = module_organisation_map.eventId AND userId = ? AND type = ? AND response = ? AND schedule = ? AND startDate > ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, organisationType.toString());
            preparedStatement.setInt(3, response);
            preparedStatement.setInt(4, schedule);
            preparedStatement.setLong(5, System.currentTimeMillis());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(organisationEventService.getByResultSet(resultSet));
                }
            }
        }
        return list;
    }

}
