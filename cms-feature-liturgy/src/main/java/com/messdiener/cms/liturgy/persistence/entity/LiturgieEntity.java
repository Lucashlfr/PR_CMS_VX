// src/main/java/com/messdiener/cms/liturgy/persistence/entity/LiturgieEntity.java
package com.messdiener.cms.liturgy.persistence.entity;

import com.messdiener.cms.shared.enums.LiturgieType;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_liturgie")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiturgieEntity {

    @Id
    @Column(name = "liturgieId", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "number", nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant", length = 4, nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "liturgieType", length = 50, nullable = false)
    private LiturgieType liturgieType;

    @Column(name = "date", nullable = false)
    private long date;

    @Column(name = "local", nullable = false)
    private boolean local;
}
