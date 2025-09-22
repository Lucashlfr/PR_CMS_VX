package com.messdiener.cms.auth.app.bootstrap;

import com.messdiener.cms.domain.auth.UserProvisioningPort;
import com.messdiener.cms.domain.person.PersonLoginDTO;
import com.messdiener.cms.domain.person.PersonLoginQueryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InMemoryUserHydrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryUserHydrator.class);

    private final PersonLoginQueryPort personLoginQueryPort;   // <- Domain-Port statt PersonService
    private final UserProvisioningPort userProvisioning;       // <- Domain-Auth-Port

    @EventListener(ApplicationReadyEvent.class)
    public void hydrateUsers() {
        List<PersonLoginDTO> persons = personLoginQueryPort.getPersonsByLogin();
        userProvisioning.initializeUsersAndPermissions(persons);
        LOGGER.info("Hydrated {} users into InMemoryUserDetailsManager", persons.size());
    }
}
