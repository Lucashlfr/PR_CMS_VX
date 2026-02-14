// cms-feature-person/.../adapters/PersonDirectoryAdapter.java
package com.messdiener.cms.person.app.adapters;

import com.messdiener.cms.domain.person.PersonDirectoryPort;
import com.messdiener.cms.domain.person.PersonOverviewLite;
import com.messdiener.cms.person.persistence.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PersonDirectoryAdapter implements PersonDirectoryPort {
    private final PersonService personService; // existiert schon

    @Override
    public List<PersonOverviewLite> getPersonsByTenant(UUID tenant) {
        return personService.getPersons().stream()
                .filter(p -> tenant == null || tenant.toString().equals(p.getTenant().toString()))
                .map(p -> new PersonOverviewLite(p.getId(), p.getFirstName(), p.getLastName(), p.getTenant()))
                .collect(Collectors.toList());
    }
}
