package com.messdiener.cms.person.app.helper;

import com.messdiener.cms.person.persistence.service.EmergencyContactService;
import com.messdiener.cms.person.persistence.service.PersonConnectionService;
import com.messdiener.cms.person.persistence.service.PersonFileService;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.domain.entity.data.EmergencyContact;
import com.messdiener.cms.person.domain.entity.data.connection.PersonConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PersonHelper {

    private final PersonFileService personFileService;
    private final PersonConnectionService connectionService;
    private final EmergencyContactService emergencyContactService;
    private final PersonService personService;

    public boolean hasPermission(Person person, int permission) {
        return person.getFRank() >= permission;
    }

    public List<PersonConnection> getConnections(Person person) {
        return connectionService.getConnectionsByHost(person.getId());
    }

    public List<EmergencyContact> getEmergencyContacts(Person person) {
        return emergencyContactService.getEmergencyContactsByPerson(person.getId());
    }

    public String getImgAddress(Person person) {
        return "/dist/assets/img/demo/user-placeholder.svg";
    }

    public String getPersonSubName(PersonConnection connection) throws SQLException {
        Optional<Person> sub = personService.getPersonById(connection.getSub());
        if (sub.isPresent()) {
            return sub.get().getName();
        }
        return "";
    }

    public String getName(UUID id) {
        return personService.getPersonName(id);
    }
}
