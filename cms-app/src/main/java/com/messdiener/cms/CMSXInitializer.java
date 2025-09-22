package com.messdiener.cms;

import com.messdiener.cms.app.infrastructure.scheduler.GlobalManager;
import com.messdiener.cms.auth.app.service.UserService;
import com.messdiener.cms.person.persistence.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class CMSXInitializer {

    private final UserService userService;
    private final GlobalManager globalManager;
    private final PersonService personService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() throws SQLException {
        globalManager.startUp();
        userService.initializeUsersAndPermissions(personService.getPersonsByLogin()); // vorherige init-Logik
    }
}
