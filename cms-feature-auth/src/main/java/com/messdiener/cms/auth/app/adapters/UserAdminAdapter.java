package com.messdiener.cms.auth.app.adapters;

import com.messdiener.cms.domain.auth.UserAdminPort;
import com.messdiener.cms.domain.auth.UserCredential;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAdminAdapter implements UserAdminPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAdminAdapter.class);

    private final PasswordEncoder passwordEncoder;
    private final InMemoryUserDetailsManager userDetailsManager;

    @Override
    public void initializeUsersAndPermissions(List<UserCredential> users) {
        for (UserCredential cred : users) {
            String username = cred.username();
            String rawPw = cred.password();

            UserDetails user = User.withUsername(username)
                    .password(passwordEncoder.encode(rawPw))
                    .roles("USER")
                    .build();

            if (userDetailsManager.userExists(username)) {
                userDetailsManager.deleteUser(username);
            }
            userDetailsManager.createUser(user);
            LOGGER.info("Initialized user '{}'", username);
        }
    }
}
