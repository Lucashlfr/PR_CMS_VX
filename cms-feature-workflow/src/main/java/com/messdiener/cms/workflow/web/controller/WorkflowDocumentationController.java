// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\controller\WorkflowDocumentationController.java
package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.DocumentationDTO;
import com.messdiener.cms.workflow.persistence.service.DocumentationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workflows/{workflowId}/docs")
@Slf4j
public class WorkflowDocumentationController {

    private final DocumentationService documentationService;
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<DocumentationDTO> create(@PathVariable UUID workflowId, @RequestBody CreateReq req) {
        log.info("[WF-DOC] create doc for workflowId={}, title={}", workflowId, req.getTitle());
        DocumentationDTO dto = new DocumentationDTO(
                UUID.randomUUID(),
                workflowId,
                req.getTitle(),
                req.getDescription(),
                req.getAssigneeId(),
                CMSDate.current()
        );
        var saved = documentationService.save(dto);

        auditService.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                workflowId,
                req.getAssigneeId(),
                "Documentation created",
                "Doc '" + saved.title() + "' angelegt"
        ));

        return ResponseEntity.ok(saved);
    }

    @Data
    public static class CreateReq {
        private String title;
        private String description;
        private UUID assigneeId;
    }
}
