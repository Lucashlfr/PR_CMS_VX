package com.messdiener.cms.v3.app.entities.tenant;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Tenant {

    private UUID id;
    private String name;
    private String color;
    private String initial;

    public List<Person> getMessdiener() throws SQLException {
        return Cache.getPersonService().getPersonsByTenant(id);
    }
}
