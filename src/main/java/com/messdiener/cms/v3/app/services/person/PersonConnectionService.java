package com.messdiener.cms.v3.app.services.person;

import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
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
public class PersonConnectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonConnectionService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_person_connection (connection_id VARCHAR(255), host VARCHAR(255), sub VARCHAR(255), type VARCHAR(255))";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PersonConnectionService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Error initializing PersonConnectionService", e);
        }
    }

    public void createConnection(PersonConnection connectionData) throws SQLException {
        String sql = "INSERT INTO module_person_connection (connection_id, host, sub, type) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, connectionData.getId().toString());
            preparedStatement.setString(2, connectionData.getHost().toString());
            preparedStatement.setString(3, connectionData.getSub().toString());
            preparedStatement.setString(4, connectionData.getConnectionType().toString());
            preparedStatement.executeUpdate();
            LOGGER.info("Connection '{}' created between '{}' and '{}'.", connectionData.getId(), connectionData.getHost(), connectionData.getSub());
        }
    }

    public List<PersonConnection> getConnectionsByHost(UUID host) throws SQLException {
        List<PersonConnection> connections = new ArrayList<>();
        String sql = "SELECT * FROM module_person_connection WHERE host = ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, host.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    UUID id = UUID.fromString(resultSet.getString("connection_id"));
                    UUID sub = UUID.fromString(resultSet.getString("sub"));
                    PersonAttributes.Connection connectionType = PersonAttributes.Connection.valueOf(resultSet.getString("type"));
                    connections.add(new PersonConnection(id, host, sub, connectionType));
                }
            }
        }

        return connections;
    }

    public void deleteConnection(UUID id) throws SQLException {
        databaseService.delete("module_person_connection", "connection_id", id.toString());
    }
}
