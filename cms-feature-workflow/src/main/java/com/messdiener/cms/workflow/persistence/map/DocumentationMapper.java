package com.messdiener.cms.workflow.persistence.map;

import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.DocumentationDTO;
import com.messdiener.cms.workflow.persistence.entity.DocumentationEntity;

public final class DocumentationMapper {
    private DocumentationMapper() {}

    public static DocumentationDTO toDto(DocumentationEntity e) {
        return new DocumentationDTO(
                e.getDocumentationId(),
                e.getWorkflowId(),
                e.getTitle(),
                e.getDescription(),
                e.getAssigneeId(),
                CMSDate.of(e.getCreatedAt() != null ? e.getCreatedAt() : 0L)
        );
    }

    public static DocumentationEntity toEntity(DocumentationDTO d) {
        return DocumentationEntity.builder()
                .documentationId(d.documentationId())
                .workflowId(d.workflowId())
                .title(d.title())
                .description(d.description())
                .assigneeId(d.assigneeId())
                .createdAt(d.createdAt() != null ? d.createdAt().toLong() : System.currentTimeMillis())
                .build();
    }
}
