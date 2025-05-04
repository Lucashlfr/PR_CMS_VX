package com.messdiener.cms.v3.app.helper.organisation;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.organisation.OrganisationMappingService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganisationEventHelper {

    private final OrganisationMappingService organisationMappingService;
    private final PersonService personService;

    public double getRegistrationPercentage(UUID eventId, UUID tenantId, int step) throws SQLException {
        int maxPersons = personService.countActivePersonsByTenant(tenantId);
        int registered = organisationMappingService.getRegisteredPersons(eventId).size();
        int scheduled = organisationMappingService.getScheduledPersons(eventId).size();
        int noFeedback = maxPersons - registered - scheduled;

        return switch (step) {
            case 1 -> ((double) scheduled / maxPersons) * 100;
            case 2 -> ((double) registered / maxPersons) * 100;
            case 3 -> ((double) noFeedback / maxPersons) * 100;
            default -> -1;
        };
    }

    public int getRegistration(UUID eventId, UUID tenantId, int step) throws SQLException {
        return switch (step) {
            case 1 -> organisationMappingService.getScheduledPersons(eventId).size();
            case 2 -> organisationMappingService.getRegisteredPersons(eventId).size();
            case 3 ->
                    personService.countActivePersonsByTenant(tenantId) - organisationMappingService.getRegisteredPersons(eventId).size() - organisationMappingService.getScheduledPersons(eventId).size();
            default -> -1;
        };
    }

    public boolean isPersonUnavailable(UUID personId, UUID eventId) throws SQLException {
        return !organisationMappingService.isRegistered(personId, eventId);
    }

    public String getScheduledPersonsInfo(UUID eventId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (Person person : organisationMappingService.getScheduledPersons(eventId)) {
            sb.append(person.getName()).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
    }

    public boolean isRegistered(UUID personId, UUID eventId) throws SQLException {
        return organisationMappingService.isRegistered(personId, eventId);
    }

    public boolean isScheduled(UUID personId, UUID eventId) throws SQLException {
        return organisationMappingService.isScheduled(personId, eventId);
    }

}
