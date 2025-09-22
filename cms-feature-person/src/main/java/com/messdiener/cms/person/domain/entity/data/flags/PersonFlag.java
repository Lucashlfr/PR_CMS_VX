package com.messdiener.cms.person.domain.entity.data.flags;

import com.messdiener.cms.shared.enums.person.FlagType;
import com.messdiener.cms.utils.time.CMSDate;
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
