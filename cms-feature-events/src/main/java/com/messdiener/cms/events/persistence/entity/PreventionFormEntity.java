// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\PreventionFormEntity.java
package com.messdiener.cms.events.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "prevention_forms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PreventionFormEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private UUID id;

    @Lob @Column(name = "structural_concerns")
    private String structuralConcerns;

    @Lob @Column(name = "toilet_signage")
    private String toiletSignage;

    @Lob @Column(name = "room_visibility")
    private String roomVisibility;

    @Lob @Column(name = "welcome_round")
    private String welcomeRound;

    @Lob @Column(name = "photo_policy")
    private String photoPolicy;

    @Lob @Column(name = "complaint_channels")
    private String complaintChannelsJson;

    @Lob @Column(name = "one_on_one_situations")
    private String oneOnOneSituations;

    @Lob @Column(name = "hierarchical_dependencies")
    private String hierarchicalDependencies;

    @Lob @Column(name = "communication_channels")
    private String communicationChannelsJson;

    @Lob @Column(name = "decision_transparency")
    private String decisionTransparencyJson;

    @Column(name = "created_at")
    private Long createdAt;
}
