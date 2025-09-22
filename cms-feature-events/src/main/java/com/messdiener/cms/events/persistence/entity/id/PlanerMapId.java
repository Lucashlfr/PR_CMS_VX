// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\id\PlanerMapId.java
package com.messdiener.cms.events.persistence.entity.id;

import lombok.*;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PlanerMapId implements Serializable {
    private UUID planerId;
    private UUID userId;
}
