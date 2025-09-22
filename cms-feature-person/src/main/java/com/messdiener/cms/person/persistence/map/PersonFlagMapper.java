// .../persistence/map/EmergencyContactMapper.java
package com.messdiener.cms.person.persistence.map;

import com.messdiener.cms.person.domain.entity.data.flags.PersonFlag;
import com.messdiener.cms.person.persistence.entity.PersonFlagEntity;

import java.util.UUID;

public final class PersonFlagMapper {
    private PersonFlagMapper(){}

    public static PersonFlagEntity toEntity(PersonFlag d, UUID personId){
        return PersonFlagEntity.builder()
                .id(d.getId()).personId(personId)
                .flagType(d.getFlagType().name())
                .flagDetails(d.getFlagDetails())
                .additionalInformation(d.getAdditionalInformation())
                .flagDate(d.getFlagDate().toLong())
                .complained(d.isComplained())
                .build();
    }
    public static PersonFlag toDomain(PersonFlagEntity e){
        return new PersonFlag(
                e.getId(),
                e.getNumber() == null ? 0 : e.getNumber(),
                com.messdiener.cms.shared.enums.person.FlagType.valueOf(e.getFlagType()),
                e.getFlagDetails(), e.getAdditionalInformation(),
                com.messdiener.cms.utils.time.CMSDate.of(e.getFlagDate()),
                e.isComplained()
        );
    }
}
