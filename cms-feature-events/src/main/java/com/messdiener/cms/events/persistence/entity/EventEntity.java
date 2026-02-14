// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\EventEntity.java
package com.messdiener.cms.events.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "module_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventEntity {

    @Id
    @Column(name = "eventId", length = 36, nullable = false)
    private UUID eventId;

    @Column(name = "tenant", length = 4, nullable = false)
    private String tenant;

    @Column(name = "number")
    private Integer number;

    @Lob @Column(name = "title")
    private String title;

    @Lob @Column(name = "description")
    private String description;

    @Column(name = "eventType", length = 50)
    private String eventType;

    @Column(name = "eventState", length = 50)
    private String eventState;

    @Column(name = "startDate")
    private Long startDate;

    @Column(name = "endDate")
    private Long endDate;

    @Column(name = "deadline")
    private Long deadline;

    @Column(name = "creationDate")
    private Long creationDate;

    @Column(name = "resubmission")
    private Long resubmission;

    @Column(name = "lastUpdate")
    private Long lastUpdate;

    @Lob @Column(name = "schedule")
    private String schedule;

    @Lob @Column(name = "registrationRelease")
    private String registrationRelease;

    @Lob @Column(name = "targetGroup")
    private String targetGroup;

    @Lob @Column(name = "location")
    private String location;

    @Lob @Column(name = "imgUrl")
    private String imgUrl;

    @Column(name = "riskIndex")
    private Integer riskIndex;

    @Column(name = "currentEditor", length = 36)
    private UUID currentEditor;

    @Column(name = "createdBy", length = 36)
    private UUID createdBy;

    @Column(name = "updatedBy", length = 36)
    private UUID updatedBy;

    @Column(name = "principal", length = 36)
    private UUID principal;

    @Column(name = "manager", length = 36)
    private UUID manager;

    @Column(name = "expenditure")
    private Double expenditure;

    @Column(name = "revenue")
    private Double revenue;

    @Lob @Column(name = "pressRelease")
    private String pressRelease;

    @Lob @Column(name = "preventionConcept")
    private String preventionConcept;

    @Lob @Column(name = "notes")
    private String notes;

    @Lob @Column(name = "application")
    private String application;

    @Lob @Column(name = "forms")
    private String forms;
}
