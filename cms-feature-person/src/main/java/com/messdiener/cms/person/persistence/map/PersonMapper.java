// cms-feature-person/src/main/java/com/messdiener/cms/person/persistence/map/PersonMapper.java
package com.messdiener.cms.person.persistence.map;

import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.entity.PersonEntity;
import com.messdiener.cms.shared.enums.PersonAttributes;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.Optional;

public final class PersonMapper {
    private PersonMapper(){}

    public static Person toDomain(PersonEntity e) {
        Optional<CMSDate> birth =
                (e.getBirthdate() == null || e.getBirthdate() == 0) ? Optional.empty() : Optional.of(CMSDate.of(e.getBirthdate()));
        Optional<CMSDate> acc =
                (e.getAccessionDate() == null || e.getAccessionDate() == 0) ? Optional.empty() : Optional.of(CMSDate.of(e.getAccessionDate()));
        Optional<CMSDate> exit =
                (e.getExitDate() == null || e.getExitDate() == 0) ? Optional.empty() : Optional.of(CMSDate.of(e.getExitDate()));
        CMSDate lastUpd = CMSDate.of(e.getLastUpdate() != null ? e.getLastUpdate() : System.currentTimeMillis());

        return new Person(
                e.getId(),
                Tenant.valueOf(e.getTenant()),
                e.getType() != null ? PersonAttributes.Type.valueOf(e.getType()) : PersonAttributes.Type.NULL,
                e.getRank() != null ? PersonAttributes.Rank.valueOf(e.getRank()) : PersonAttributes.Rank.NULL,
                e.getPrincipal(),
                e.getFRank(),
                e.getSalutation() != null ? PersonAttributes.Salutation.valueOf(e.getSalutation()) : PersonAttributes.Salutation.NULL,
                e.getFirstname(),
                e.getLastname(),
                e.getGender() != null ? PersonAttributes.Gender.valueOf(e.getGender()) : PersonAttributes.Gender.NOT_SPECIFIED,
                birth,
                e.getStreet(),
                e.getHouseNumber(),
                e.getPostalCode(),
                e.getCity(),
                e.getEmail(),
                e.getPhone(),
                e.getMobile(),
                acc,
                exit,
                e.getActivityNote(),
                e.getNotes(),
                e.isActive(),
                e.isCanLogin(),
                e.getUsername(),
                e.getPassword(),
                e.getIban(),
                e.getBic(),
                e.getBank(),
                e.getAccountHolder(),
                e.isCustomPassword(),
                lastUpd
        );
    }

    public static PersonEntity toEntity(Person d) {
        return PersonEntity.builder()
                .id(d.getId())
                .tenant(d.getTenant().toString())
                .type(d.getType() != null ? d.getType().toString() : null)
                .rank(d.getRank() != null ? d.getRank().toString() : null)
                .principal(d.getPrincipal())
                .fRank(d.getFRank())
                .salutation(d.getSalutation() != null ? d.getSalutation().toString() : null)
                .firstname(d.getFirstname())
                .lastname(d.getLastname())
                .gender(d.getGender() != null ? d.getGender().toString() : null)
                .birthdate(d.getBirthdate().map(CMSDate::toLong).orElse(0L))
                .street(d.getStreet())
                .houseNumber(d.getHouseNumber())
                .postalCode(d.getPostalCode())
                .city(d.getCity())
                .email(d.getEmail())
                .phone(d.getPhone())
                .mobile(d.getMobile())
                .accessionDate(d.getAccessionDate().map(CMSDate::toLong).orElse(0L))
                .exitDate(d.getExitDate().map(CMSDate::toLong).orElse(0L))
                .activityNote(d.getActivityNote())
                .notes(d.getNotes())
                .active(d.isActive())
                .canLogin(d.isCanLogin())
                .username(d.getUsername())
                .password(d.getPassword())
                .iban(d.getIban())
                .bic(d.getBic())
                .bank(d.getBank())
                .accountHolder(d.getAccountHolder())
                .customPassword(d.isCustomPassword())
                .lastUpdate(System.currentTimeMillis())
                .build();
    }
}
