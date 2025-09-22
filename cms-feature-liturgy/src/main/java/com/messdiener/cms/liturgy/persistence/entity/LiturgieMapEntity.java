// src/main/java/com/messdiener/cms/liturgy/persistence/entity/LiturgieMapEntity.java
package com.messdiener.cms.liturgy.persistence.entity;

import com.messdiener.cms.shared.enums.LiturgieState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "module_liturgie_map")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiturgieMapEntity {

    @EmbeddedId
    private LiturgieMapId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 255, nullable = false)
    private LiturgieState state;
}
