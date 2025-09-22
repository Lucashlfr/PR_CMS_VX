package com.messdiener.cms.workflow.persistence.map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.WorkflowTaskDTO;
import com.messdiener.cms.workflow.persistence.entity.WorkflowTaskEntity;

import java.util.Collections;
import java.util.List;

public final class WorkflowTaskMapper {
    private static final ObjectMapper M = new ObjectMapper();
    private WorkflowTaskMapper() {}

    private static String listToJson(List<String> roles) {
        try { return roles == null ? "[]" : M.writeValueAsString(roles); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    @SuppressWarnings("unchecked")
    private static List<String> jsonToList(String json) {
        try { return json == null || json.isBlank()
                ? Collections.emptyList()
                : M.readValue(json, new TypeReference<List<String>>(){}); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    public static WorkflowTaskDTO toDto(WorkflowTaskEntity e) {
        return new WorkflowTaskDTO(
                e.getId(),
                e.getWorkflowId(),
                e.getKey(),
                e.getTitle(),
                e.getState(),
                jsonToList(e.getCandidateRolesJson()),
                e.getAssigneeId(),
                CMSDate.of(e.getDueAt() != null ? e.getDueAt() : 0L),
                CMSDate.of(e.getCreatedAt() != null ? e.getCreatedAt() : 0L),
                Jsons.toMap(e.getPayloadJson())
        );
    }

    public static WorkflowTaskEntity toEntity(WorkflowTaskDTO d) {
        return WorkflowTaskEntity.builder()
                .id(d.id())
                .workflowId(d.workflowId())
                .key(d.key())
                .title(d.title())
                .state(d.state())
                .candidateRolesJson(listToJson(d.candidateRoles()))
                .assigneeId(d.assigneeId())
                .dueAt(d.dueAt() != null ? d.dueAt().toLong() : null)
                .createdAt(d.createdAt() != null ? d.createdAt().toLong() : System.currentTimeMillis())
                .payloadJson(Jsons.toJson(d.payload()))
                .build();
    }
}
