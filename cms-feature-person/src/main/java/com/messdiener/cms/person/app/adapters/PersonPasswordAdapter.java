package com.messdiener.cms.person.app.adapters;

import com.messdiener.cms.domain.person.PersonPasswordCommandPort;
import com.messdiener.cms.domain.person.PersonPasswordQueryPort;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PersonPasswordAdapter implements PersonPasswordQueryPort, PersonPasswordCommandPort {

    private final PersonService personService;

    @Override
    public boolean hasCustomPassword(String username) {
        Optional<Person> person = personService.getPersonByUsername(username);
        return person.map(Person::isCustomPassword).orElse(false);
    }

    @Override
    public void setPasswordAndMarkCustom(String username, String encodedPassword) {
        Person person = personService.getPersonByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));
        person.setPassword(encodedPassword);
        person.setCustomPassword(true);
        personService.updatePerson(person);
    }
}
