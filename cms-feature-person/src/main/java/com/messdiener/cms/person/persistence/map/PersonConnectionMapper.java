// .../persistence/map/EmergencyContactMapper.java
package com.messdiener.cms.person.persistence.map;

import com.messdiener.cms.person.domain.entity.data.connection.PersonConnection;
import com.messdiener.cms.person.persistence.entity.PersonConnectionEntity;

public final class PersonConnectionMapper {
    private PersonConnectionMapper() {
    }

    public static PersonConnectionEntity toEntity(PersonConnection d) {
        return PersonConnectionEntity.builder()
                .id(d.getId()).host(d.getHost()).sub(d.getSub())
                .type(d.getConnectionType().name()).build();
    }

    public static PersonConnection toDomain(PersonConnectionEntity e) {
        return new PersonConnection(e.getId(), e.getHost(), e.getSub(),
                com.messdiener.cms.shared.enums.PersonAttributes.Connection.valueOf(e.getType()));
    }
}
