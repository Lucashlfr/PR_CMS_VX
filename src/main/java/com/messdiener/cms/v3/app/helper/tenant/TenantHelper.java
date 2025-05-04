package com.messdiener.cms.v3.app.helper.tenant;

import com.messdiener.cms.v3.app.entities.organisation.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.organisation.OrganisationAnalyticsService;
import com.messdiener.cms.v3.app.services.organisation.OrganisationEventService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.tenant.TenantService;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantHelper {

    private final PersonService personService;
    private final TenantService tenantService;
    private final OrganisationEventService organisationEventService;
    private final OrganisationAnalyticsService organisationAnalyticsService;


    public List<Person> getMessdiener(UUID tenant) {
        try {
            return personService.getPersonsByTenant(tenant);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<OrganisationEvent> getWorshipEvents(UUID tenant) {
        try {
            return organisationEventService.getNextEvents(tenant, OrganisationType.WORSHIP);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public int getEventSize(UUID tenant) {
        try {
            return getWorshipEvents(tenant).size() * getMessdiener(tenant).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getScheduledPersons(UUID tenant) {
        try {
            return organisationAnalyticsService.getResponses(tenant, 1, 1);
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getRegisteredPersons(UUID tenant) {
        try {
            return organisationAnalyticsService.getResponses(tenant, 1, 0);
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getOtherPersons(UUID tenant) {
        try {
            return getEventSize(tenant) - getScheduledPersons(tenant) - getRegisteredPersons(tenant);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getPercentageRegister(UUID tenant, int state) {
        int total = getEventSize(tenant);
        if (total == 0) return 0;

        return switch (state) {
            case 1 -> (double) getScheduledPersons(tenant) / total * 100;
            case 0 -> (double) getRegisteredPersons(tenant) / total * 100;
            default -> (double) getOtherPersons(tenant) / total * 100;
        };
    }

    public Optional<String> getTenantName(Tenant tenant) {
        try {
            return tenantService.findTenant(tenant.getId()).map(Tenant::getName);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Tenant getTenant(UUID id) throws SQLException {
        return tenantService.findTenant(id).orElse(new Tenant(UUID.randomUUID(), "NULL", "NULL", "NULL"));
    }

}
