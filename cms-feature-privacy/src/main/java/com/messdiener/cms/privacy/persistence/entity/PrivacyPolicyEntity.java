// X:\workspace\PR_CMS\cms-feature-privacy\src\main\java\com\messdiener\cms\privacy\persistence\entity\PrivacyPolicyEntity.java
package com.messdiener.cms.privacy.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "module_privacy_policy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrivacyPolicyEntity {

    // Im Alt-Schema ist "id" VARCHAR, inhaltlich aber UUID -> als UUID mappen.
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private UUID id;

    @Column(name = "date")
    private Long date; // epoch ms (ALT: LONG) :contentReference[oaicite:1]{index=1}

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "street")
    private String street;

    @Column(name = "houseNumber")
    private String number;

    @Column(name = "plz")
    private String plz;

    @Column(name = "town")
    private String city;

    @Column(name = "o1")
    private Boolean check1;

    @Column(name = "o2")
    private Boolean check2;

    @Column(name = "o3")
    private Boolean check3;

    @Column(name = "o4")
    private Boolean check4;

    @Column(name = "o5")
    private Boolean check5;

    @Column(name = "o6")
    private Boolean check6;

    @Column(name = "o7")
    private Boolean check7;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)     // Hibernate 6: als CLOB/LONGTEXT behandeln
    @Column(name = "signature", columnDefinition = "LONGTEXT")
    private String signature;
}
