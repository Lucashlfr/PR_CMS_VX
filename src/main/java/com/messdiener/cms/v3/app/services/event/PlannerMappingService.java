package com.messdiener.cms.v3.app.services.event;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
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
public class PlannerMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerMappingService.class);
    private final DatabaseService databaseService;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        try {
            createTable("CREATE TABLE IF NOT EXISTS module_planer_principal (planerId VARCHAR(255), userId VARCHAR(255))");
            createTable("CREATE TABLE IF NOT EXISTS module_planer_map (planerId VARCHAR(255), userId VARCHAR(255), date LONG)");
            LOGGER.info("PlannerMappingService initialized and tables ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerMappingService", e);
        }
    }

    private void createTable(String sql) throws SQLException {
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    public void savePrincipal(UUID planerId, List<UUID> userIds) throws SQLException {
        databaseService.delete("module_planer_principal", "planerId", planerId.toString());
        String sql = "INSERT INTO module_planer_principal (planerId, userId) VALUES (?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            for (UUID userId : userIds) {
                preparedStatement.setString(2, userId.toString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            LOGGER.info("Saved {} principal(s) for planer '{}'.", userIds.size(), planerId);
        }
    }

    public List<Person> getPrincipals(UUID planerId) throws SQLException {
        String sql = "SELECT * FROM module_person, module_planer_principal WHERE planerId = ? AND userId = person_id ORDER BY lastname";
        return getPeople(planerId, sql);
    }

    public void updateEventParticipants(UUID eventId, List<UUID> checkedPersons) throws SQLException {
        databaseService.delete("module_planer_map", "planerId", eventId.toString());
        String sql = "INSERT INTO module_planer_map (planerId, userId, date) VALUES (?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            long timestamp = System.currentTimeMillis();
            preparedStatement.setString(1, eventId.toString());
            preparedStatement.setLong(3, timestamp);
            for (UUID personId : checkedPersons) {
                preparedStatement.setString(2, personId.toString());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            LOGGER.info("Mapped {} participant(s) to event '{}'.", checkedPersons.size(), eventId);
        }
    }

    public List<Person> getMappedPersons(UUID planerId) throws SQLException {
        String sql = "SELECT * FROM module_planer_map, module_person WHERE planerId = ? AND userId = person_id ORDER BY lastname DESC";
        return getPeople(planerId, sql);
    }

    private List<Person> getPeople(UUID planerId, String sql) throws SQLException {
        List<Person> persons = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(personService.getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }


    public boolean personIsMapped(UUID planerId, UUID personId) throws SQLException {
        String sql = "SELECT 1 FROM module_planer_map WHERE planerId = ? AND userId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            preparedStatement.setString(2, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
