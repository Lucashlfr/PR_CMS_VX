package com.messdiener.cms.v3.app.helper.event;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.organisation.OrganisationMappingService;
import com.messdiener.cms.v3.shared.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganisationEventHelper {

    private final OrganisationMappingService organisationMappingService;

    public double getRegistrationPercentage(UUID eventId, UUID tenantId, int step) throws SQLException {
        int maxPersons = 100; //TODO: FIX tenantService.findTenant(tenantId).orElseThrow().getMessdiener().size();
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
}
