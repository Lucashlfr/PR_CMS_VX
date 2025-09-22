// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\PlanerPrincipalEntity.java
package com.messdiener.cms.events.persistence.entity;

import com.messdiener.cms.events.persistence.entity.id.PlanerPrincipalId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "module_planer_principal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanerPrincipalEntity {

    @EmbeddedId
    private PlanerPrincipalId id;
}
