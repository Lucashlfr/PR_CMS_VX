package com.messdiener.cms.v3.app.request;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Data
public class ImportRequest {

    private List<List<String>> records;

    public static ImportRequest createRequest(List<List<String>> records) {
        return new ImportRequest(records);
    }

    public String importFunction() throws SQLException {

        for(List<String> record : records){
            UUID uuid = UUID.fromString(record.get(0));
            long timestamp = Long.parseLong(record.get(1));

            Optional<Person> person = Cache.getPersonService().getPersonById(uuid);
            if(person.isEmpty()){
                continue;
            }
            person.get().setBirthdate(Optional.of(CMSDate.of(timestamp)));
            person.get().update();
        }

        return "Import wurde erfolgreich durchgeführt (" + records.size() + ")";
    }


}
