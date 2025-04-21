package com.messdiener.cms.v3.security;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.other.CharacterConverter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityHelper.class);
    private final Cache cache;
    private final PersonHelper personHelper;

    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public Optional<Person> getPerson() {
        try {
            return cache.getPersonService().getPersonByUsername(getUsername());
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch person by username: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<Person> addPersonToSession(HttpSession httpSession) {
        try {
            Optional<Person> personOpt = getPerson();

            personOpt.ifPresent(person -> {
                httpSession.setAttribute("sessionUser", person);
                httpSession.setAttribute("serviceName", "CMSX");
                httpSession.setAttribute("tenantName", personHelper.getTenantName(person).orElse(""));
                httpSession.setAttribute("img", personHelper.getImgAddress());
                httpSession.setAttribute("openWorkflows", -1);
                httpSession.setAttribute("permissionTag", "TEAM,ADMIN,GLT");
            });

            return personOpt;
        } catch (Exception e) {
            LOGGER.warn("Session update failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
