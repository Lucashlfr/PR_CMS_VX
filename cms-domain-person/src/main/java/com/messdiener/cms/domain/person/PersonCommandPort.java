package com.messdiener.cms.domain.person;


import com.messdiener.cms.shared.enums.PersonAttributes;

import java.time.LocalDate;
import java.util.UUID;

public interface PersonCommandPort {
    void updateGeneral(UUID id, PersonAttributes.Salutation salutation, String firstname, String lastname,
                       PersonAttributes.Gender gender, String phone, String mobile, String mail);

    void updateDates(UUID id, LocalDate birthdate, LocalDate accessionDate, LocalDate exitDate);

    void updateAccountAndManager(UUID id, PersonAttributes.Type type, PersonAttributes.Rank rank, int fRank, UUID principal);

    void updateAddress(UUID id, String street, String houseNumber, String postalCode, String city);

    void updateBank(UUID id, String iban, String bic, String bank, String accountHolder);
}
