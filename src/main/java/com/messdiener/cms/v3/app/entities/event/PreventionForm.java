package com.messdiener.cms.v3.app.entities.event;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PreventionForm {

    private UUID id;

    // Category 1: Locations and Surroundings
    private String structuralConcerns;
    private String toiletSignage;
    private String roomVisibility;

    // Category 2: Awareness and Complaints
    private String welcomeRound;
    private String photoPolicy;
    private List<String> complaintChannels;

    // Category 3: One-on-One Situations
    private String oneOnOneSituations;

    // Category 4: Power and Dependency
    private String hierarchicalDependencies;

    // Category 5: Communication and Transparency
    private List<String> communicationChannels;
    private List<String> decisionTransparency;

}