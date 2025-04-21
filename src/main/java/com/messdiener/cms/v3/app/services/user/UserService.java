package com.messdiener.cms.v3.app.services.user;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.user.Permission;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final DatabaseService databaseService;
    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;
    private final InMemoryUserDetailsManager userDetailsManager;

    @PostConstruct
    public void init() {
        LOGGER.info("UserService initialized.");
    }

    public void createSingleUser(Person person) {
        try {
            UserDetails user = User.withUsername(person.getUsername())
                    .password(passwordEncoder.encode(person.getPassword()))
                    .roles("USER")
                    .build();

            if (userDetailsManager.userExists(person.getUsername())) {
                userDetailsManager.deleteUser(person.getUsername());
                LOGGER.info("Deleted existing user '{}'.", person.getUsername());
            }

            userDetailsManager.createUser(user);
            LOGGER.info("Created user '{}' in security context.", person.getUsername());

        } catch (Exception e) {
            LOGGER.error("Failed to create user '{}': {}", person.getUsername(), e.getMessage(), e);
        }
    }

    public void initializeUsersAndPermissions() {
        try {
            personService.getPersonsByLogin().forEach(this::createSingleUser);
            insertDefaultPermissionsBatch();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize users and permissions: {}", e.getMessage(), e);
        }
    }

    private void insertDefaultPermissionsBatch() {
        String sql = "INSERT INTO module_user_permission (permName, permDescr, type, id) VALUES (?, ?, ?, ?)";

        try (var connection = databaseService.getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {

            for (Permission p : Permission.getDefaultPermissions()) {
                if (databaseService.exists("module_user_permission", "permName", p.getName())) {
                    continue;
                }

                preparedStatement.setString(1, p.getName());
                preparedStatement.setString(2, p.getDescription());
                preparedStatement.setString(3, p.getType());
                preparedStatement.setInt(4, p.getId());
                preparedStatement.addBatch();

                LOGGER.debug("Prepared batch insert for permission '{}'.", p.getName());
            }

            preparedStatement.executeBatch();
            LOGGER.info("Batch insert for default permissions executed.");

        } catch (SQLException e) {
            LOGGER.error("Failed to batch insert permissions: {}", e.getMessage(), e);
        }
    }
}
