package com.messdiener.cms.person.app.adapters;

import com.messdiener.cms.domain.person.PersonLoginDTO;
import com.messdiener.cms.domain.person.PersonLoginQueryPort;
import com.messdiener.cms.person.persistence.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonLoginQueryAdapter implements PersonLoginQueryPort {

    private final PersonService personService;

    @Override
    public List<PersonLoginDTO> getPersonsByLogin() {
        return personService.getPersonsByLogin();
    }
}
