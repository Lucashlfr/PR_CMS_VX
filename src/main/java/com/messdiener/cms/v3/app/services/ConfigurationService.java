package com.messdiener.cms.v3.app.services;

import com.messdiener.cms.v3.app.services.sql.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigurationService {

	private final DatabaseService databaseService;

	public ConfigurationService(DatabaseService databaseService) {
		this.databaseService = databaseService;

		try {
			databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_configuration (tag VARCHAR(255), value VARCHAR(255))").executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(String tag, String value) throws SQLException {
		databaseService.delete("module_configuration", "tag", tag);
		PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_configuration (tag, value) VALUES (?,?)");
		preparedStatement.setString(1, tag);
		preparedStatement.setString(2, value);
		preparedStatement.executeUpdate();
	}

	public String get(String tag) throws SQLException {
		PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_configuration WHERE tag = ?");
		preparedStatement.setString(1, tag);
		ResultSet resultSet = preparedStatement.executeQuery();
		if(resultSet.next())
			return resultSet.getString("value");
		return ("No value found for tag " + tag);
	}

	public boolean isPresent(String tag) throws SQLException {
		PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_configuration WHERE tag = ?");
		preparedStatement.setString(1, tag);
		ResultSet resultSet = preparedStatement.executeQuery();
		return resultSet.next();
	}

}
