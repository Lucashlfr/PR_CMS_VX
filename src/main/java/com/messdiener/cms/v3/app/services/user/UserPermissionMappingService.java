package com.messdiener.cms.v3.app.services.user;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.user.Permission;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserPermissionMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPermissionMappingService.class);
    private final DatabaseService databaseService;
    private final PersonService personService;
    private final PermissionService permissionService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_user_permissions_map (user_uuid VARCHAR(255), permName VARCHAR(255))";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("UserPermissionMappingService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize UserPermissionMappingService", e);
        }
    }

    public void mapPermissions(Person person, String permission, boolean state) throws SQLException {
        String sql = state ?
                "INSERT INTO module_user_permissions_map (user_uuid, permName) VALUES (?, ?)" :
                "DELETE FROM module_user_permissions_map WHERE user_uuid = ? AND permName = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, person.getId().toString());
            preparedStatement.setString(2, permission);
            preparedStatement.executeUpdate();
            LOGGER.info("{} permission '{}' for user '{}'.", state ? "Assigned" : "Removed", permission, person.getId());
        }
    }

    public boolean hasPermission(Person person, String permission) throws SQLException {
        String sql = "SELECT 1 FROM module_user_permissions_map WHERE user_uuid = ? AND permName = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, person.getId().toString());
            preparedStatement.setString(2, permission);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    //TODO: FIXXX
    public boolean userHasPermission(Person person, String permission, boolean strict) throws SQLException {
        //if (!strict && hasPermission(person, "*")) return true;
        //return hasPermission(person, permission);
        return true;
    }

    public void removeAllPermissionFromUser(UUID id) {
        databaseService.delete("module_user_permissions_map", "user_uuid", id.toString());
        LOGGER.info("All permissions removed for user '{}'.", id);
    }
    //TODO: FIX
    public List<Permission> getPermissionsByUser(Person person) throws SQLException {
        List<Permission> allPermissions = permissionService.getPermissions();
        List<Permission> checkedPermissions = new ArrayList<>();
        for (Permission permission : allPermissions) {
            //if (person.hasPermissionStrict(permission.getName())) {
            //    checkedPermissions.add(permission);
            //}
        }
        return checkedPermissions;
    }
    //TODO: FIX
    public List<Permission> getUncheckPermissionsByUser(Person person) throws SQLException {
        List<Permission> allPermissions = permissionService.getPermissions();
        List<Permission> uncheckedPermissions = new ArrayList<>();
        for (Permission permission : allPermissions) {
            //if (!person.hasPermission(permission.getName())) {
            //    uncheckedPermissions.add(permission);
            //}
        }
        return uncheckedPermissions;
    }
}
