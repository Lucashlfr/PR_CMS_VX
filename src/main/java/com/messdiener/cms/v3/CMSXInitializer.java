package com.messdiener.cms.v3;

import com.messdiener.cms.v3.app.services.person.PersonLoginService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.user.UserService;
import com.messdiener.cms.v3.shared.scheduler.GlobalManager;
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
    private final PersonLoginService personLoginService;
    private final PersonService personService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() throws SQLException {
        globalManager.startUp();
        userService.initializeUsersAndPermissions(personService.getPersonsByLogin()); // vorherige init-Logik
    }
}
