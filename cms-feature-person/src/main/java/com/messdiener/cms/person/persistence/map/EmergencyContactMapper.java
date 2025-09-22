// .../persistence/map/EmergencyContactMapper.java
package com.messdiener.cms.person.persistence.map;

import com.messdiener.cms.person.domain.entity.data.EmergencyContact;
import com.messdiener.cms.person.persistence.entity.EmergencyContactEntity;

import java.util.UUID;

public final class EmergencyContactMapper {
    private EmergencyContactMapper(){}

    public static EmergencyContactEntity toEntity(EmergencyContact d, UUID personId) {
        return EmergencyContactEntity.builder()
                .id(d.getContactId())
                .personId(personId)
                .type(d.getType())
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .phoneNumber(d.getPhoneNumber())
                .mail(d.getMail())
                .active(d.isActive())
                .build();
    }

    public static EmergencyContact toDomain(EmergencyContactEntity e) {
        return new EmergencyContact(e.getId(), e.getType(), e.getFirstName(), e.getLastName(), e.getPhoneNumber(), e.getMail(), e.isActive());
    }
}
