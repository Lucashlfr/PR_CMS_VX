// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\EventMessageEntity.java
package com.messdiener.cms.events.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "module_event_message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventMessageEntity {

    @Id
    @Column(name = "messageId", length = 36, nullable = false)
    private UUID messageId;

    @Column(name = "eventId", length = 36, nullable = false)
    private UUID eventId;

    // Alt-Schema hat AUTO_INCREMENT PRIMARY KEY 'number', jedoch wird in der Domain auch eine 'id' geführt.
    @Column(name = "number")
    private Integer number;

    @Lob @Column(name = "title")
    private String title;

    @Lob @Column(name = "description")
    private String description;

    @Column(name = "date")
    private Long date;

    @Column(name = "type", length = 30)
    private String type;

    @Column(name = "userId", length = 36)
    private UUID userId;
}
