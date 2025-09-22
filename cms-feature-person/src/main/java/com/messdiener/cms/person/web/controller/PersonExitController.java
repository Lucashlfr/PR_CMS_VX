package com.messdiener.cms.person.web.controller;

import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PersonExitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonExitController.class);
    private final SecurityHelper securityHelper;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        LOGGER.info("PersonExitController initialized.");
    }

    @PostMapping("/personal/state")
    public RedirectView quit(@RequestParam("id") UUID id, @RequestParam("state") String state) throws SQLException {
        UUID userId = securityHelper.getPerson().map(com.messdiener.cms.domain.person.PersonSessionView::id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Person person = personService.getPersonById(id).orElseThrow();

        if(state.equals("active")) {
            person.setActive(true);
            person.setCanLogin(true);
        }else if(state.equals("inactive")) {
            person.setActive(false);
            person.setActivityNote("[" + user.getName() + "] [" + CMSDate.current().getGermanDate() + "] ");
            person.setCanLogin(false);
        }
        personService.updatePerson(person);

        return new RedirectView("/personal?q=profil&s=settings&id=" + id);
    }

}
