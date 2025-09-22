// src/main/java/com/messdiener/cms/liturgy/persistence/entity/LiturgieRequestMapId.java
package com.messdiener.cms.liturgy.persistence.entity;

import lombok.*;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class LiturgieRequestMapId implements Serializable {
    private UUID requestId;
    private UUID personId;
}
