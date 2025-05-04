package com.messdiener.cms.v3.app.entities.person;

import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Person {

    private UUID id;
    private UUID tenantId;

    private PersonAttributes.Type type;
    private PersonAttributes.Rank rank;
    private UUID principal;
    private int fRank;

    private PersonAttributes.Salutation salutation;
    private String firstname;
    private String lastname;

    private PersonAttributes.Gender gender;
    private Optional<CMSDate> birthdate;

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;

    private String email;
    private String phone;
    private String mobile;

    private Optional<CMSDate> accessionDate;
    private Optional<CMSDate> exitDate;
    private String activityNote;

    private String notes;

    private boolean active;

    private boolean canLogin;
    private String username;
    private String password;

    private String iban;
    private String bic;
    private String bank;
    private String accountHolder;

    private String privacyPolicy;
    private String signature;

    private boolean ob1;
    private boolean ob2;
    private boolean ob3;
    private boolean ob4;

    private CMSDate preventionDate;

    public static Person empty(UUID tenantId) {
        return new Person(UUID.randomUUID(), tenantId,
                PersonAttributes.Type.NULL, PersonAttributes.Rank.NULL, Cache.SYSTEM_USER, 4, PersonAttributes.Salutation.NULL,
                "", "", PersonAttributes.Gender.NOT_SPECIFIED, Optional.empty(),
                "", "", "", "", "", "", "", Optional.empty(), Optional.empty(),
                "", "", true, false, "", "", "", "", "", "", "", "", false, false, false, false, CMSDate.of(0));
    }

    public String getReadName() {
        return lastname + ", " + firstname;
    }

    public String getName() {
        return firstname + " " + lastname;
    }

    public String getAddress() {
        String address = street + " " + houseNumber + ", " + postalCode + " " + city;
        return address.trim().length() <= 4 ? "" : address;
    }

    public String getBankDetails() {
        String details = iban + " / " + bic + " / " + bank + " / " + accountHolder;
        if (details.trim().length() <= 9 || details.contains("null")) {
            return "";
        }
        return details;
    }
}

