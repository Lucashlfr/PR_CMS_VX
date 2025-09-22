package com.messdiener.cms.workflow.persistence.map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.WorkflowDTO;
import com.messdiener.cms.workflow.persistence.entity.WorkflowEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class WorkflowDtoMapper {

    private static final ObjectMapper M = new ObjectMapper();

    private WorkflowDtoMapper() {}

    private static String listToJson(List<String> tags) {
        try { return tags == null ? "[]" : M.writeValueAsString(tags); }
        catch (Exception e) { throw new IllegalStateException("Serialize tagsJson failed", e); }
    }

    private static List<String> jsonToList(String json) {
        try {
            return (json == null || json.isBlank())
                    ? Collections.emptyList()
                    : M.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Parse tagsJson failed", e);
        }
    }

    public static WorkflowDTO toDto(WorkflowEntity e) {
        return new WorkflowDTO(
                e.getWorkflowId(),
                e.getProcessKey(),
                Optional.ofNullable(e.getProcessVersion()).orElse(1),
                e.getTitle(),
                Optional.ofNullable(e.getDescription()).orElse(""),
                e.getState(),
                e.getAssigneeId(),
                e.getApplicantId(),
                e.getManager(),
                e.getTargetElement(),
                Optional.ofNullable(e.getPriority()).orElse(0),
                jsonToList(e.getTagsJson()),
                Optional.ofNullable(e.getAttachments()).orElse(0),
                Optional.ofNullable(e.getNotes()).orElse(0),
                CMSDate.of(Optional.ofNullable(e.getCreationDate()).orElse(0L)),
                CMSDate.of(Optional.ofNullable(e.getModificationDate()).orElse(0L)),
                CMSDate.of(Optional.ofNullable(e.getEndDate()).orElse(0L)),
                Optional.ofNullable(e.getCurrentNumber()).orElse(0),
                Optional.ofNullable(e.getMetadata()).orElse("")
        );
    }

    public static WorkflowEntity toEntity(WorkflowDTO d) {
        return WorkflowEntity.builder()
                .workflowId(d.workflowId())
                .processKey(d.processKey())
                .processVersion(d.processVersion())
                .title(d.title())
                .description(d.description())
                .state(d.state())
                .assigneeId(d.assigneeId())
                .applicantId(d.applicantId())
                .manager(d.manager())
                .targetElement(d.targetElement())
                .priority(d.priority())
                .tagsJson(listToJson(d.tags()))
                .attachments(d.attachments())
                .notes(d.notes())
                .creationDate(d.creationDate() != null ? d.creationDate().toLong() : System.currentTimeMillis())
                .modificationDate(d.modificationDate() != null ? d.modificationDate().toLong() : System.currentTimeMillis())
                .endDate(d.endDate() != null ? d.endDate().toLong() : 0L)
                .currentNumber(d.currentNumber())
                .metadata(d.metadata())
                .build();
    }
}
