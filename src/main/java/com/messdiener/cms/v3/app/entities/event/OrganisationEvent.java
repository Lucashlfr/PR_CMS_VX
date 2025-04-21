package com.messdiener.cms.v3.app.entities.event;

import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
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

    public String getTime() {
        return openEnd ? startDate.getDayDate() : startDate.getDayDate() + " bis " + endDate.getDayDate();
    }
}
