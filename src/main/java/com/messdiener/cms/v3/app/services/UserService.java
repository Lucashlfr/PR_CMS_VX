package com.messdiener.cms.v3.app.services;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.user.Permission;
import com.messdiener.cms.v3.app.entities.user.config.UserConfiguration;
import com.messdiener.cms.v3.app.entities.user.config.UserConfigurations;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {

    private static final Logger LOGGER = Logger.getLogger("Manager.UserService");
    private final DatabaseService databaseService;


    public UserService(DatabaseService databaseService) {
        this.databaseService = databaseService;

        LOGGER.setLevel(Level.ALL);
        try {
            databaseService.getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS module_user_configuration (user_uuid VARCHAR(255), name VARCHAR(255), value VARCHAR(255))"
            ).executeUpdate();

            LOGGER.finest("Database erfolgreich erstellt.");
        } catch (SQLException e) {
            LOGGER.severe("Fehler! ");
            e.printStackTrace();
        }

    }

    public void createSingleUser(Person person){

        UserDetails user = User.withUsername(person.getUsername())
                .password(Cache.getPasswordEncoder().encode(person.getPassword())) // Passwort korrekt hashen
                .roles("USER")
                .build();

        if(Cache.getUserDetailsManager().userExists(person.getUsername())){
            Cache.getUserDetailsManager().deleteUser(person.getUsername());
        }
        Cache.getUserDetailsManager().createUser(user);


    }

    public void createUserInSecurity() throws SQLException {

        Cache.getPersonService().getPersonsByLogin().forEach(this::createSingleUser);

        Permission.getDefaultPermissions().forEach(p -> {
            try {
                p.save();
            } catch (SQLException e) {
                throw new IllegalStateException("SQL konnte nicht ausgeführt werden");
            }
        });
    }

    public UserConfigurations getUserConfigurations(UUID userId) throws SQLException {
        List<UserConfiguration> configurations = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_user_configuration WHERE user_uuid = ?");
        preparedStatement.setString(1, userId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            configurations.add(getConfigurationByResult(resultSet));
        }

        return UserConfigurations.of(configurations);
    }

    private UserConfiguration getConfigurationByResult(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        String value = resultSet.getString("value");
        return new UserConfiguration(name, value);
    }

    public void saveConfigurations(Person person, String name, String value) throws SQLException {
        PreparedStatement deleteStatement = databaseService.getConnection().prepareStatement("DELETE FROM module_user_configuration WHERE user_uuid = ? AND name = ?");
        deleteStatement.setString(1, person.getId().toString());
        deleteStatement.setString(2, name);
        deleteStatement.executeUpdate();

        PreparedStatement insertStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_user_configuration (user_uuid, name, value) VALUES (?,?,?)");
        insertStatement.setString(1, person.getId().toString());
        insertStatement.setString(2, name);
        insertStatement.setString(3, value);
        insertStatement.executeUpdate();
    }

}
