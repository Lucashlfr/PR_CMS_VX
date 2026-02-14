// cms-feature-person/src/main/java/com/messdiener/cms/person/app/adapters/PersonLookupAdapter.java
package com.messdiener.cms.person.app.adapters;

import com.messdiener.cms.domain.person.PersonLookupPort;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PersonLookupAdapter implements PersonLookupPort {

    private final PersonService personService;
    private final PersonHelper personHelper;

    @Override
    public Optional<PersonSessionView> findByUsernameForSession(String username) {
        Optional<Person> personOpt = personService.getPersonByUsername(username);
        return personOpt.map(p -> new PersonSessionView(
                p.getId(),
                p.getFirstName(),                 // <- neu
                p.getLastName(),                  // <- neu
                p.getTenant().getName(),
                p.getTenant(),
                p.getFRank(),
                personHelper.getImgAddress(p)
        ));
    }
}
