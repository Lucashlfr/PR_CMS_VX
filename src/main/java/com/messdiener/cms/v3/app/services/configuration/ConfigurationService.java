package com.messdiener.cms.v3.app.services.configuration;

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

@Service
@RequiredArgsConstructor
public class ConfigurationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);
	private final DatabaseService databaseService;

	@PostConstruct
	public void init() {
		try (Connection connection = databaseService.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_configuration (tag VARCHAR(255), value VARCHAR(255))")) {
			preparedStatement.executeUpdate();
			LOGGER.info("Configuration table initialized successfully.");
		} catch (SQLException e) {
			LOGGER.error("Error while initializing configuration table", e);
			throw new RuntimeException(e);
		}
	}

	public void save(String tag, String value) throws SQLException {
		databaseService.delete("module_configuration", "tag", tag);
		try (Connection connection = databaseService.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO module_configuration (tag, value) VALUES (?, ?)")) {
			preparedStatement.setString(1, tag);
			preparedStatement.setString(2, value);
			preparedStatement.executeUpdate();
			LOGGER.info("Configuration saved for tag '{}'.", tag);
		}
	}

	public String get(String tag) throws SQLException {
		try (Connection connection = databaseService.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM module_configuration WHERE tag = ?")) {
			preparedStatement.setString(1, tag);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("value");
				} else {
					LOGGER.warn("No configuration value found for tag '{}'.", tag);
					return "No value found for tag " + tag;
				}
			}
		}
	}

	public boolean isPresent(String tag) throws SQLException {
		try (Connection connection = databaseService.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM module_configuration WHERE tag = ?")) {
			preparedStatement.setString(1, tag);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}
}
