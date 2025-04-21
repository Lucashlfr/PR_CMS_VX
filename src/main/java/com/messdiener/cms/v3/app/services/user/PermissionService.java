package com.messdiener.cms.v3.app.services.user;

import com.messdiener.cms.v3.app.entities.user.Permission;
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

@Service
@RequiredArgsConstructor
public class PermissionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionService.class);
	private final DatabaseService databaseService;

	@PostConstruct
	public void init() {
		String sql = "CREATE TABLE IF NOT EXISTS module_user_permission (permName VARCHAR(255), permDescr VARCHAR(255), type VARCHAR(255), id INT, PRIMARY KEY (permName))";
		try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.executeUpdate();
			LOGGER.info("PermissionService initialized and table ensured.");
		} catch (SQLException e) {
			LOGGER.error("Failed to initialize PermissionService", e);
		}
	}

	public void createPermission(Permission permission) throws SQLException {
		if (databaseService.exists("module_user_permission", "permName", permission.getName())) return;

		String sql = "INSERT INTO module_user_permission (permName, permDescr, type, id) VALUES (?, ?, ?, ?)";
		try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, permission.getName());
			preparedStatement.setString(2, permission.getDescription());
			preparedStatement.setString(3, permission.getType());
			preparedStatement.setInt(4, permission.getId());
			preparedStatement.executeUpdate();
			LOGGER.info("Permission '{}' created.", permission.getName());
		}
	}

	public void deletePermission(Permission permission) {
		databaseService.delete("module_user_permission", "permName", permission.getName());
		LOGGER.info("Permission '{}' deleted.", permission.getName());
	}

	public List<Permission> getPermissions() throws SQLException {
		List<Permission> permissions = new ArrayList<>();
		String sql = "SELECT * FROM module_user_permission ORDER BY permName";
		try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
			 ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				permissions.add(getPermissionFromResultSet(resultSet));
			}
		}
		return permissions;
	}

	private Permission getPermissionFromResultSet(ResultSet resultSet) throws SQLException {
		String name = resultSet.getString("permName");
		String description = resultSet.getString("permDescr");
		String type = resultSet.getString("type");
		int id = resultSet.getInt("id");
		return Permission.of(name, type, description, id);
	}
}
