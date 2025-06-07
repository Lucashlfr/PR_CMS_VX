package com.messdiener.cms.v3.app.entities.person.data.flags;

import com.messdiener.cms.v3.shared.enums.person.FlagType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PersonFlag {

    private UUID id;
    private int number;
    private FlagType flagType;
    private String flagDetails;
    private String additionalInformation;
    private CMSDate flagDate;
    private boolean complained;
}
