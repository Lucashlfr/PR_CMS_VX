// cms-feature-person/.../persistence/entity/EmergencyContactEntity.java
package com.messdiener.cms.person.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_person_emergency_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactEntity {

    @Id
    @Column(name = "contact_id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "person_id", nullable = false, length = 36)
    private UUID personId;

    @Column(name = "type")
    private String type;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "mail")
    private String mail;

    @Column(name = "active", nullable = false)
    private boolean active;
}
