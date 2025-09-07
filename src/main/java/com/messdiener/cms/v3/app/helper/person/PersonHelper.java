package com.messdiener.cms.v3.app.helper.person;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.data.EmergencyContact;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.services.person.EmergencyContactService;
import com.messdiener.cms.v3.app.services.person.PersonConnectionService;
import com.messdiener.cms.v3.app.services.person.PersonFileService;
import com.messdiener.cms.v3.app.services.person.PersonService;
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
        try {
            return connectionService.getConnectionsByHost(person.getId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<EmergencyContact> getEmergencyContacts(Person person) {
        try {
            return emergencyContactService.getEmergencyContactsByPerson(person.getId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
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

    public String getName(UUID id) throws SQLException {
        return personService.getPersonName(id);
    }
}
