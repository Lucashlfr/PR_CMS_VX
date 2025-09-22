// src/main/java/com/messdiener/cms/liturgy/persistence/entity/LiturgieRequestEntity.java
package com.messdiener.cms.liturgy.persistence.entity;

import com.messdiener.cms.shared.enums.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_liturgie_request")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiturgieRequestEntity {

    @Id
    @Column(name = "requestId", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant", length = 4, nullable = false)
    private Tenant tenant;

    @Column(name = "creatorId", length = 36, nullable = false)
    private UUID creatorId;

    @Column(name = "number", nullable = false)
    private int number;

    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(name = "startDate", nullable = false)
    private long startDate;

    @Column(name = "endDate", nullable = false)
    private long endDate;

    @Column(name = "deadline", nullable = false)
    private long deadline;

    @Column(name = "active", nullable = false)
    private boolean active;
}
