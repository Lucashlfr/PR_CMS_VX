// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\EventResultEntity.java
package com.messdiener.cms.events.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "module_events_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventResultEntity {

    @Id
    @Column(name = "resultId", length = 36, nullable = false)
    private UUID resultId;

    @Column(name = "eventId", length = 36, nullable = false)
    private UUID eventId;

    @Column(name = "userId", length = 36)
    private UUID userId;

    @Column(name = "date")
    private Long date;

    @Lob @Column(name = "json")
    private String json;
}
