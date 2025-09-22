// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\map\PreventionFormMapper.java
package com.messdiener.cms.events.persistence.map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.events.domain.entity.PreventionForm;
import com.messdiener.cms.events.persistence.entity.PreventionFormEntity;

import java.util.Collections;
import java.util.List;

public final class PreventionFormMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private PreventionFormMapper(){}

    public static PreventionForm toDomain(PreventionFormEntity e){
        PreventionForm f = new PreventionForm();
        f.setId(e.getId());
        f.setStructuralConcerns(e.getStructuralConcerns());
        f.setToiletSignage(e.getToiletSignage());
        f.setRoomVisibility(e.getRoomVisibility());
        f.setWelcomeRound(e.getWelcomeRound());
        f.setPhotoPolicy(e.getPhotoPolicy());
        f.setComplaintChannels(fromJsonList(e.getComplaintChannelsJson()));
        f.setOneOnOneSituations(e.getOneOnOneSituations());
        f.setHierarchicalDependencies(e.getHierarchicalDependencies());
        f.setCommunicationChannels(fromJsonList(e.getCommunicationChannelsJson()));
        f.setDecisionTransparency(fromJsonList(e.getDecisionTransparencyJson()));
        return f;
    }

    public static PreventionFormEntity toEntity(PreventionForm f){
        return PreventionFormEntity.builder()
                .id(f.getId())
                .structuralConcerns(f.getStructuralConcerns())
                .toiletSignage(f.getToiletSignage())
                .roomVisibility(f.getRoomVisibility())
                .welcomeRound(f.getWelcomeRound())
                .photoPolicy(f.getPhotoPolicy())
                .complaintChannelsJson(toJson(f.getComplaintChannels()))
                .oneOnOneSituations(f.getOneOnOneSituations())
                .hierarchicalDependencies(f.getHierarchicalDependencies())
                .communicationChannelsJson(toJson(f.getCommunicationChannels()))
                .decisionTransparencyJson(toJson(f.getDecisionTransparency()))
                .createdAt(System.currentTimeMillis())
                .build();
    }

    private static List<String> fromJsonList(String json){
        if(json == null || json.isBlank()) return Collections.emptyList();
        try { return MAPPER.readValue(json, new TypeReference<>(){}); }
        catch (Exception ex){ return Collections.emptyList(); }
    }
    private static String toJson(List<String> list){
        try { return list == null ? "[]" : MAPPER.writeValueAsString(list); }
        catch (Exception ex){ return "[]"; }
    }
}
