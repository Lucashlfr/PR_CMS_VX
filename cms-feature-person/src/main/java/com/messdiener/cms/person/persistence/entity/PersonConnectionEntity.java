package com.messdiener.cms.person.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_person_connection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonConnectionEntity {
    @Id
    @Column(name = "connection_id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "host", nullable = false, length = 36)
    private UUID host;

    @Column(name = "sub", nullable = false, length = 36)
    private UUID sub;

    @Column(name = "type", nullable = false, length = 64)
    private String type; // PersonAttributes.Connection als String
}
