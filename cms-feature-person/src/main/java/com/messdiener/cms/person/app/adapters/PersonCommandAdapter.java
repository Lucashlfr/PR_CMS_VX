package com.messdiener.cms.person.app.adapters;

import com.messdiener.cms.domain.person.PersonCommandPort;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.PersonAttributes;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PersonCommandAdapter implements PersonCommandPort {

    private final PersonService personService;

    @Override
    public void updateGeneral(UUID id,
                              PersonAttributes.Salutation salutation,
                              String firstname,
                              String lastname,
                              PersonAttributes.Gender gender,
                              String phone,
                              String mobile,
                              String mail) {
        Person p = load(id);
        p.setSalutation(salutation);
        p.setFirstName(trimOrNull(firstname));
        p.setLastName(trimOrNull(lastname));
        p.setGender(gender);
        p.setPhone(trimOrNull(phone));
        p.setMobile(trimOrNull(mobile));
        p.setEmail(trimOrNull(mail));
        personService.updatePerson(p);
    }

    @Override
    public void updateDates(UUID id,
                            LocalDate birthdate,
                            LocalDate accessionDate,
                            LocalDate exitDate) {
        Person p = load(id);
        p.setBirthdate(toCmsDate(birthdate));
        p.setAccessionDate(toCmsDate(accessionDate));
        p.setExitDate(toCmsDate(exitDate));
        personService.updatePerson(p);
    }

    @Override
    public void updateAccountAndManager(UUID id,
                                        PersonAttributes.Type type,
                                        PersonAttributes.Rank rank,
                                        int fRank,
                                        UUID principal) {
        Person p = load(id);
        p.setType(type);
        p.setRank(rank);
        p.setFRank(fRank);
        p.setPrincipal(principal);
        personService.updatePerson(p);
    }

    @Override
    public void updateAddress(UUID id,
                              String street,
                              String houseNumber,
                              String postalCode,
                              String city) {
        Person p = load(id);
        p.setStreet(trimOrNull(street));
        p.setHouseNumber(trimOrNull(houseNumber));
        p.setPostalCode(trimOrNull(postalCode));
        p.setCity(trimOrNull(city));
        personService.updatePerson(p);
    }

    @Override
    public void updateBank(UUID id,
                           String iban,
                           String bic,
                           String bank,
                           String accountHolder) {
        Person p = load(id);
        p.setIban(trimOrNull(iban));
        p.setBic(trimOrNull(bic));
        p.setBank(trimOrNull(bank));
        p.setAccountHolder(trimOrNull(accountHolder));
        personService.updatePerson(p);
    }

    // -------------------- helpers --------------------

    private Person load(UUID id) {
        return personService.getPersonById(id)
                .orElseThrow(() -> new IllegalArgumentException("Person not found: " + id));
    }

    private Optional<CMSDate> toCmsDate(LocalDate d) {
        if (d == null) return Optional.empty();
        long epochMillis = d.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return Optional.of(CMSDate.of(epochMillis));
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
