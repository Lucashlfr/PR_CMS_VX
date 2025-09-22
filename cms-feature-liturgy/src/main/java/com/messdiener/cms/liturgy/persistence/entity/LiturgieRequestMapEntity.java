// src/main/java/com/messdiener/cms/liturgy/persistence/entity/LiturgieRequestMapEntity.java
package com.messdiener.cms.liturgy.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "module_liturgie_request_map")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiturgieRequestMapEntity {

    @EmbeddedId
    private LiturgieRequestMapId id;

    @Column(name = "date", nullable = false)
    private long date;
}
