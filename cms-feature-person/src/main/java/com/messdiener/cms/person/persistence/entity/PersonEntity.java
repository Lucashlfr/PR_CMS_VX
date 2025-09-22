// cms-feature-person/src/main/java/com/messdiener/cms/person/persistence/entity/PersonEntity.java
package com.messdiener.cms.person.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_person")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonEntity {

    @Id
    @Column(name = "person_id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "tenant", length = 4, nullable = false)
    private String tenant;

    @Column(name = "type")
    private String type;

    @Column(name = "person_rank")
    private String rank;

    @Column(name = "principal", length = 36, nullable = false)
    private UUID principal;

    @Column(name = "fRank")
    private Integer fRank;

    @Column(name = "salutation")
    private String salutation;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "gender")
    private String gender;

    // LONG in DB -> als epoch millis
    @Column(name = "birthdate")
    private Long birthdate;

    @Column(name = "street")
    private String street;

    @Column(name = "houseNumber")
    private String houseNumber;

    @Column(name = "postalCode")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "accessionDate")
    private Long accessionDate;

    @Column(name = "exitDate")
    private Long exitDate;

    @Lob
    @Column(name = "activityNote")
    private String activityNote;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "canLogin", nullable = false)
    private boolean canLogin;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "iban")
    private String iban;

    @Column(name = "bic")
    private String bic;

    @Column(name = "bank")
    private String bank;

    @Column(name = "accountHolder")
    private String accountHolder;

    @Lob
    @Column(name = "privacy_policy")
    private String privacyPolicy;

    @Lob
    @Column(name = "signature")
    private byte[] signature;

    @Column(name = "ob1")
    private Boolean ob1;

    @Column(name = "ob2")
    private Boolean ob2;

    @Column(name = "ob3")
    private Boolean ob3;

    @Column(name = "ob4")
    private Boolean ob4;

    @Column(name = "preventionDate")
    private Long preventionDate;

    @Column(name = "customPassword", nullable = false)
    private boolean customPassword;

    @Column(name = "lastUpdate")
    private Long lastUpdate;

    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (fRank == null) fRank = 0;
    }


}
