package com.messdiener.cms.v3.web.controller.personal;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final PersonHelper personHelper;
    private final SecurityHelper securityHelper;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        LOGGER.info("PersonExitController initialized.");
    }

    @PostMapping("/personal/state")
    public RedirectView quit(@RequestParam("id") UUID id, @RequestParam("state") String state) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
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
