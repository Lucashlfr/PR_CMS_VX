package com.messdiener.cms.app.infrastructure.scheduler;

import com.messdiener.cms.person.persistence.service.PersonFlagService;
import com.messdiener.cms.person.persistence.service.PersonLoginService;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GlobalManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalManager.class);

    private final PersonService personService;
    private final PersonLoginService personLoginService;

    private final UUID systemUserId = UUID.fromString("93dacda6-b951-413a-96dc-9a37858abe3e");
    private final PersonFlagService personFlagService;


    @PostConstruct
    public void logInit() {
        LOGGER.info("GlobalManager initialized.");
    }

    public void startUp() throws SQLException {
        personLoginService.matchPersonToUser(); // <- hier aufrufen

        for(Person person : personService.getPersons()){
            personFlagService.createDefaultFlags(person.getId());
        }

    }

}
