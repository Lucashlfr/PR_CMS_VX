package com.messdiener.cms.v3.app.services.user;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.security.SecurityHelper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

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
            //LOGGER.info("Created user '{}' in security context.", person.getUsername());

        } catch (Exception e) {
            LOGGER.error("Failed to create user '{}': {}", person.getUsername(), e.getMessage(), e);
        }
    }

    public void initializeUsersAndPermissions() {
        try {
            personService.getPersonsByLogin().forEach(this::createSingleUser);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize users and permissions: {}", e.getMessage(), e);
        }
    }

}
