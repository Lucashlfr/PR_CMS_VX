package com.messdiener.cms.v3.app.entities.event;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class OrganisationEvent {

    private UUID id;
    private UUID tenantId;
    private OrganisationType organisationType;

    private CMSDate startDate;
    private CMSDate endDate;
    private boolean openEnd;

    private String description;
    private String info;
    private String metaData;

    public String getTime(){
        return openEnd ? startDate.getDayDate() : startDate.getDayDate() + " bis " + endDate.getDayDate();
    }

    public List<Person> getScheduledPersons() throws SQLException {
        return Cache.getOrganisationService().getScheduledPersons(this.id);
    }

    public List<Person> getRegisteredPersons() throws SQLException {
        return Cache.getOrganisationService().getRegisteredPersons(this.id);
    }


    public List<Person> getPresentPersons(){
        return new ArrayList<>();
    }

    public List<Person> getExcusedPersons(){
        return new ArrayList<>();
    }

    public void save() throws SQLException {
        Cache.getOrganisationService().saveEvent(this);
    }

    // Eingeteilten
    // Angemeldeten
    // keine RÜckmeldung

    public double getPercentagesRegister(int step) throws SQLException {
        int maxPersons = Cache.getTenantService().findTenant(tenantId).orElseThrow().getMessdiener().size();
        int register = getRegisteredPersons().size();
        int scheduled = getScheduledPersons().size();
        int noFeedback = maxPersons - register - scheduled;

        switch (step){
            case 1:
                return ((double) scheduled / maxPersons * 100);

            case 2:
                return ((double) register / maxPersons * 100);

            case 3:
                return ((double) noFeedback / maxPersons * 100);

            default:
                return -1;
        }
    }

    public int getOtherPersons() throws SQLException {
        int maxPersons = Cache.getTenantService().findTenant(tenantId).orElseThrow().getMessdiener().size();
        int register = getRegisteredPersons().size();
        int scheduled = getScheduledPersons().size();
        return maxPersons - register - scheduled;
    }

    public String getScheduledPersonsInfo() throws SQLException {
        StringBuilder sb = new StringBuilder();
        for(Person person : getScheduledPersons()){
            sb.append(person.getName()).append(", ");
        }

        if(sb.length() > 0)
                return sb.substring(0, sb.length() - 2);
        return "";
    }


}
