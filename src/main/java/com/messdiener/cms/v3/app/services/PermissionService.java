package com.messdiener.cms.v3.app.services;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.user.Permission;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionService {

	private final DatabaseService databaseService;

	public PermissionService(DatabaseService databaseService) {
		this.databaseService = databaseService;

		try {
			databaseService.getConnection().prepareStatement(
					"CREATE TABLE IF NOT EXISTS module_user_permission (permName VARCHAR(255), permDescr VARCHAR(255), type VARCHAR(255), id int, PRIMARY KEY (permName))"
			).executeUpdate();

			databaseService.getConnection().prepareStatement(
					"CREATE TABLE  IF NOT EXISTS  module_user_permissions_map (user_uuid VARCHAR(255), permName VARCHAR(255))"
			).executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void createPermission(Permission permission) throws SQLException {

		if(databaseService.exists("module_user_permission", "permName", permission.getName()))return;

		PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement(
				"INSERT INTO module_user_permission (permName, permDescr, type, id) VALUES (?, ?,?,?)"
		);
		preparedStatement.setString(1, permission.getName());
		preparedStatement.setString(2, permission.getDescription());
		preparedStatement.setString(3, permission.getType());
		preparedStatement.setInt(4, permission.getId());

		preparedStatement.executeUpdate();
	}

	public void deletePermission(Permission permission){
		databaseService.delete("module_user_permission", "permName", permission.getName());
	}

	public List<Permission> getPermissions() throws SQLException {
		List<Permission> permissions = new ArrayList<>();
		ResultSet resultSet = databaseService.getConnection().prepareStatement("SELECT * FROM module_user_permission ORDER BY permName").executeQuery();
		while (resultSet.next()) {

			String name = resultSet.getString("permName");
			String description = resultSet.getString("permDescr");
			String type = resultSet.getString("type");
			int id = resultSet.getInt("id");

			Permission permission = new Permission(name, type, description, id);
			permissions.add(permission);
		}
		return permissions;
	}

	public void mapPermissions(Person person, String permission, boolean state) throws SQLException {

		PreparedStatement preparedStatement;
		if(state){
			preparedStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_user_permissions_map (user_uuid, permName) VALUES (?,?)");
		}else {
			preparedStatement = databaseService.getConnection().prepareStatement("DELETE FROM module_user_permissions_map WHERE user_uuid = ? AND permName = ?");
		}
		preparedStatement.setString(1, person.getId().toString());
		preparedStatement.setString(2, permission);
		preparedStatement.executeUpdate();
	}

	private boolean userHasePermission1(Person person, String permission) throws SQLException {
		PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_user_permissions_map WHERE user_uuid = ? AND permName = ?");
		preparedStatement.setString(1, person.getId().toString());
		preparedStatement.setString(2, permission);

		ResultSet resultSet = preparedStatement.executeQuery();

		return resultSet.next();
	}

	public boolean userHasePermission(Person person, String permission, boolean strict) throws SQLException {
		if(!strict && userHasePermission1(person, "*"))return true;
		return userHasePermission1(person, permission);
	}

	public void removeAllPermissionFromUser(UUID id) {
		databaseService.delete("module_user_permissions_map", "user_uuid", id.toString());
	}

	public List<Permission> getUncheckPermissionsByUser(Person person) throws SQLException {

		List<Permission> allPermissions = getPermissions();
		List<Permission> uncheckPermissions = new ArrayList<>();

		for(Permission p : allPermissions){
			if(person.hasPermission(p.getName()))continue;
			uncheckPermissions.add(p);
		}
		return uncheckPermissions;
	}

	public List<Permission> getPermissionsByUser(Person person) throws SQLException {
		List<Permission> allPermissions = getPermissions();
		List<Permission> checkPermissions = new ArrayList<>();

		for(Permission p : allPermissions){
			if(person.hasPermissionStrict(p.getName())) checkPermissions.add(p);
		}
		return checkPermissions;
	}
}
