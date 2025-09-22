// X:\workspace\PR_CMS\cms-feature-person\src\main\java\com\messdiener\cms\person\persistence\entity\PersonFlagEntity.java
package com.messdiener.cms.person.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.util.UUID;

@Entity
@Table(name = "module_person_flag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonFlagEntity {

    @Id
    @Column(name = "flagId", nullable = false, length = 36)
    private UUID id;

    @Column(name = "person_id", nullable = false, length = 36)
    private UUID personId;

    // Von der DB generiert (z. B. AUTO_INCREMENT), aber KEIN @Id!
    // insertable/updatable = false, damit JPA nichts mitschickt.
    // Optional: @Generated(INSERT), damit Hibernate den Wert nach dem Insert neu liest.
    @Generated(GenerationTime.INSERT)
    @Column(name = "number", updatable = false, insertable = false)
    private Integer number;

    @Column(name = "flagType", nullable = false)
    private String flagType;

    @Lob
    @Column(name = "flagDetails")
    private String flagDetails;

    @Lob
    @Column(name = "additionalInformation")
    private String additionalInformation;

    @Column(name = "flagDate", nullable = false)
    private long flagDate;

    @Column(name = "complained", nullable = false)
    private boolean complained;
}
