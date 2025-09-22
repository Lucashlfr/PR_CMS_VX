// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\PlanerMapEntity.java
package com.messdiener.cms.events.persistence.entity;

import com.messdiener.cms.events.persistence.entity.id.PlanerMapId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "module_planer_map")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanerMapEntity {

    @EmbeddedId
    private PlanerMapId id;

    @Column(name = "date")
    private Long date; // EPOCH ms
}
