package com.messdiener.cms.v3.app.entities.planer;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.planer.entities.PlannerLog;
import com.messdiener.cms.v3.app.entities.planer.tasks.PlanerTask;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.planer.PlanerState;
import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
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
public class PlanerEvent {

    private UUID planerId;
    private UUID tenantId;
    private int id;

    private Optional<UUID> managerId;

    private String eventName;
    private CMSDate startDate;
    private String imgUrl;

    private PlanerState planerState;

    private List<PlanerTask> tasks;
    private List<Person> editors;

    public static PlanerEvent create(UUID tenantId){
        return new PlanerEvent(UUID.randomUUID(), tenantId, 0,Optional.empty(), "", CMSDate.current(), "", PlanerState.CREATED, new ArrayList<>(), new ArrayList<>());
    }

}
