package com.messdiener.cms.v3.app.entities.finance;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
@Data
public class TransactionEntry {

    private UUID id;
    private UUID tenant;
    private UUID creator;
    private String type;

    private CMSDate documentDate;

    private String costCenter;
    private String description;
    private String note;

    private double value;

    private boolean exchange;

    private boolean active;

    public static TransactionEntry empty(Person person) {
        return new TransactionEntry(UUID.randomUUID(), person.getTenantId(), person.getId(), "", CMSDate.current(), "", "", "", 0d, false, true);
    }

    public void save() throws SQLException {
        Cache.getFinanceService().saveFinanceRequest(this);
    }

    public Person getCreatorUser() throws SQLException {
        return Cache.getPersonService().getPersonById(creator).orElseThrow();
    }

    public String getValueString(){
        String v = "" + value;
        if(v.endsWith(".0")){
            return v + "0";
        }
        return v;
    }
}
