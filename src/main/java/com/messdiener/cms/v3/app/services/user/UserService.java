package com.messdiener.cms.v3.app.services.user;

import com.messdiener.cms.v3.app.entities.person.dto.PersonLoginDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;
    private final InMemoryUserDetailsManager userDetailsManager;

    @PostConstruct
    public void init() {
        LOGGER.info("UserService initialized.");
    }

    public void createSingleUser(PersonLoginDTO login) {
        try {

            UserDetails user = User.withUsername(login.getUsername())
                    .password(passwordEncoder.encode(login.getPassword()))
                    .roles("USER")
                    .build();

            if (userDetailsManager.userExists(login.getUsername())) {
                userDetailsManager.deleteUser(login.getUsername());
            }

            userDetailsManager.createUser(user);

        } catch (Exception e) {
            LOGGER.error("Failed to initialize users and permissions: {}", e.getMessage(), e);
        }
    }

    public void initializeUsersAndPermissions(List<PersonLoginDTO> persons) {
        try {
            persons.forEach(this::createSingleUser);
            LOGGER.info("Created user '{}'.", persons.size());
        } catch (Exception e) {
            LOGGER.error("Failed to initialize users and permissions: {}", e.getMessage(), e);
        }
    }

}
