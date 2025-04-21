package com.messdiener.cms.v3.app.services.user;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.user.config.UserConfiguration;
import com.messdiener.cms.v3.app.entities.user.config.UserConfigurations;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
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
public class UserConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConfigurationService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_user_configuration (user_uuid VARCHAR(255), name VARCHAR(255), value VARCHAR(255))";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("UserConfigurationService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize UserConfigurationService", e);
        }
    }

    public UserConfigurations getUserConfigurations(UUID userId) throws SQLException {
        List<UserConfiguration> configurations = new ArrayList<>();
        String sql = "SELECT * FROM module_user_configuration WHERE user_uuid = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    configurations.add(getConfigurationByResult(resultSet));
                }
            }
        }
        return UserConfigurations.of(configurations);
    }

    public void saveConfigurations(Person person, String name, String value) throws SQLException {
        String deleteSql = "DELETE FROM module_user_configuration WHERE user_uuid = ? AND name = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            preparedStatement.setString(1, person.getId().toString());
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
        }

        String insertSql = "INSERT INTO module_user_configuration (user_uuid, name, value) VALUES (?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            preparedStatement.setString(1, person.getId().toString());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, value);
            preparedStatement.executeUpdate();
            LOGGER.info("Saved configuration '{}' for user '{}'", name, person.getId());
        }
    }

    private UserConfiguration getConfigurationByResult(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        String value = resultSet.getString("value");
        return new UserConfiguration(name, value);
    }
}
