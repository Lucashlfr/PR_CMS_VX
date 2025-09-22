// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\EventComponentEntity.java
package com.messdiener.cms.events.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "module_events_components")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eventId", length = 36, nullable = false)
    private UUID eventId;

    @Column(name = "number")
    private Integer number;

    @Column(name = "type", length = 255)
    private String type;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "value", length = 255)
    private String value;

    @Lob @Column(name = "options")
    private String options;

    @Column(name = "required")
    private Boolean required;
}
