package com.messdiener.cms.v3.app.services.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventApplicationService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {

        try (Connection connection = databaseService.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_events_components (eventId VARCHAR(255), number integer, type VARCHAR(255), name VARCHAR(255), label VARCHAR(255), value VARCHAR(255), options TEXT, required boolean)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_events_components table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_events_results (eventId VARCHAR(255), date long, json LONGTEXT )")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_events_results table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }

    }

    public void deleteComponent(UUID eventId, int number) throws SQLException {
        String sql = "DELETE FROM module_events_components where eventId = ? and number = ?";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, eventId.toString());
            preparedStatement.setInt(2, number);
            preparedStatement.executeUpdate();
        }
    }

    public void saveComponent(UUID eventId, Component component) throws SQLException {
        deleteComponent(eventId, component.getNumber());

        String sql = "INSERT INTO module_events_components (eventId, number, type, name, label, value, options, required) VALUES (?,?,?,?,?,?,?,?)";
        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, eventId.toString());
            preparedStatement.setInt(2, component.getNumber());
            preparedStatement.setString(3, component.getType().toString());
            preparedStatement.setString(4, component.getName());
            preparedStatement.setString(5, component.getLabel());
            preparedStatement.setString(6, component.getValue());
            preparedStatement.setString(7, component.getOptions());
            preparedStatement.setBoolean(8, component.isRequired());
            preparedStatement.executeUpdate();
        }
    }

    private Component getComponentByResultSet(ResultSet resultSet) throws SQLException {
        int number = resultSet.getInt("number");
        ComponentType type = ComponentType.valueOf(resultSet.getString("type"));
        String name =  resultSet.getString("name");
        String label =   resultSet.getString("label");
        String value =  resultSet.getString("value");
        String options =   resultSet.getString("options");
        boolean required =   resultSet.getBoolean("required");
        return new Component(number, type, name, label, value, options, required, new ArrayList<>());
    }

    public List<Component> getComponents(UUID eventId) throws SQLException {
        List<Component> components = new ArrayList<>();
        String sql = "SELECT * FROM module_events_components WHERE eventId = ? ORDER BY number";
        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, eventId.toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    components.add(getComponentByResultSet(resultSet));
                }
            }
        }
        return components;
    }

    public void saveResult(UUID eventId, String result) throws SQLException {
        String sql = "INSERT INTO module_events_results (eventId, date, json) VALUES (?,?,?)";

        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, eventId.toString());
            preparedStatement.setLong(2, System.currentTimeMillis());
            preparedStatement.setString(3, result);
            preparedStatement.executeUpdate();
        }
    }

    public HashMap<Long, String> getResults(UUID eventId) throws SQLException {
        HashMap<Long, String> results = new HashMap<>();
        String sql = "SELECT * FROM module_events_results WHERE eventId = ? ORDER BY date";
        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, eventId.toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    results.put(resultSet.getLong(1), resultSet.getString(2));
                }
            }
        }
        return results;
    }

    public List<String[]> exportEventResults(UUID eventId) throws SQLException, JsonProcessingException {
        List<String[]> rows = new ArrayList<>();
        List<String> componentNames = new ArrayList<>();
        List<String> header = new ArrayList<>();

        // Komponenten abrufen
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT name, label FROM module_events_components WHERE eventId = ? ORDER BY number ASC")) {
            preparedStatement.setString(1, eventId.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    componentNames.add(resultSet.getString("name"));
                    header.add(resultSet.getString("label"));
                }
            }
        }

        rows.add(header.toArray(new String[0]));

        // Ergebnisse abrufen und JSON mappen
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT json FROM module_events_results WHERE eventId = ?")) {
            preparedStatement.setString(1, eventId.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ObjectMapper objectMapper = new ObjectMapper();
                while (resultSet.next()) {
                    String json = resultSet.getString("json");

                    Map<String, Object> dataMap = objectMapper.readValue(json, new TypeReference<>() {});
                    String[] row = componentNames.stream()
                            .map(name -> Optional.ofNullable(dataMap.get(name)).orElse("").toString())
                            .toArray(String[]::new);
                    rows.add(row);
                }
            }
        }

        return rows;
    }



}
