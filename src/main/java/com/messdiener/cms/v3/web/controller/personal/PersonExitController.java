package com.messdiener.cms.v3.web.controller.personal;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
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
    private final Cache cache;
    private final PersonHelper personHelper;
    private final SecurityHelper securityHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("PersonExitController initialized.");
    }

    @PostMapping("/personal/deregister")
    public RedirectView deregister(@RequestParam("id") UUID id, @RequestParam("reason") String reason) throws SQLException {
        Person person = cache.getPersonService().getPersonById(id).orElseThrow();

        person.setActive(false);

        cache.getPersonService().updatePerson(person);
        return new RedirectView("/personal?q=profil&id=" + person.getId());
    }

    @PostMapping("/personal/quit")
    public RedirectView quit(@RequestParam("id") UUID id, @RequestParam("reason") String reason) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person person = cache.getPersonService().getPersonById(id).orElseThrow();

        person.setActive(false);
        person.setActivityNote("[" + user.getName() + "] [" + CMSDate.current().getGermanDate() + "] " + reason);
        cache.getPersonService().updatePerson(person);

        return new RedirectView("/personal?q=profil&s=1&id=" + id);
    }

    @GetMapping("/personal/join")
    public RedirectView join(@RequestParam("id") UUID id) throws SQLException {
        Person person = cache.getPersonService().getPersonById(id).orElseThrow();

        person.setActive(true);
        cache.getPersonService().updatePerson(person);
        return new RedirectView("/personal?q=profil&s=1&id=" + id);
    }

}
