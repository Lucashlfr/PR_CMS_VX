// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\controller\WorkflowActionsController.java
package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.app.service.WorkflowNotificationApplicationService;
import com.messdiener.cms.workflow.app.service.WorkflowTaskApplicationService;
import com.messdiener.cms.workflow.domain.dto.WorkflowTaskDTO;
import com.messdiener.cms.workflow.persistence.service.ArtifactService;
import com.messdiener.cms.workflow.persistence.service.WorkflowAuditTrailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workflows/{workflowId}/actions")
@Slf4j
public class WorkflowActionsController {

    private final WorkflowTaskApplicationService taskApp;
    private final WorkflowNotificationApplicationService notificationApp;
    private final ArtifactService artifactService;
    private final WorkflowAuditTrailService auditTrail;

    /* -------- TASK: Schnellanlage direkt am Workflow -------- */
    @PostMapping("/task")
    public ResponseEntity<WorkflowTaskDTO> quickTask(@PathVariable UUID workflowId, @RequestBody QuickTaskReq req) {
        log.info("[WF-ACTIONS] quick task create, workflowId={}, title={}", workflowId, req.getTitle());
        WorkflowTaskDTO dto = taskApp.createManualTask(
                workflowId,
                req.getTitle(),
                req.getState() == null ? CMSState.OPEN : req.getState(),
                req.getAssigneeId(),
                req.getDueAt(),
                req.getPayload()
        );
        auditTrail.logSimple(workflowId, req.getActorId(), "TASK_QUICK_CREATE",
                Map.of("title", req.getTitle()), Map.of("taskId", dto.id().toString()));
        return ResponseEntity.ok(dto);
    }

    /* -------- NOTIFICATION: generisch an Workflow -------- */
    @PostMapping("/notification")
    public ResponseEntity<Map<String, Object>> quickNotification(@PathVariable UUID workflowId, @RequestBody QuickNotificationReq req) {
        log.info("[WF-ACTIONS] quick notification, workflowId={}, title={}", workflowId, req.getTitle());
        notificationApp.notifyWorkflow(workflowId, req.getTitle(), req.getBody());
        auditTrail.logSimple(workflowId, req.getActorId(), "NOTIFICATION_CREATE",
                Map.of("title", req.getTitle()), Map.of("scope", "workflow"));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /* -------- UPLOAD: Artefakt (manuell) registrieren -------- */
    @PostMapping(path = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> upload(@PathVariable UUID workflowId,
                                                      @RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "actorId", required = false) UUID actorId) {
        log.info("[WF-ACTIONS] upload file, workflowId={}, filename={}, size={}",
                workflowId, file == null ? "null" : file.getOriginalFilename(), file == null ? -1 : file.getSize());

        // Hier wird nur das Artefakt am Workflow registriert (Storage macht dein Dokumentenmodul)
        var artifact = artifactService.addDocumentArtifact(workflowId, actorId);
        Map<String, Object> meta = new LinkedHashMap<>();
        if (file != null) {
            meta.put("filename", file.getOriginalFilename());
            meta.put("contentType", file.getContentType());
            meta.put("size", file.getSize());
        }

        auditTrail.logSimple(workflowId, actorId, "ARTIFACT_UPLOADED",
                null, Map.of("artifactId", artifact.getArtifactId().toString(), "file", meta));

        return ResponseEntity.ok(Map.of("ok", true, "artifactId", artifact.getArtifactId().toString()));
    }

    /* -------- AUDITLOG: manueller Eintrag -------- */
    @PostMapping("/audit")
    public ResponseEntity<Map<String, Object>> audit(@PathVariable UUID workflowId, @RequestBody ManualAuditReq req) {
        log.info("[WF-ACTIONS] manual audit, workflowId={}, action={}", workflowId, req.getAction());
        auditTrail.logSimple(workflowId, req.getActorId(), req.getAction(), req.getBefore(), req.getAfter());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /* ====== DTOs ====== */

    @Data
    public static class QuickTaskReq {
        private String title;
        private CMSState state;
        private UUID assigneeId;
        private CMSDate dueAt;
        private Map<String, Object> payload;
        private UUID actorId;
    }

    @Data
    public static class QuickNotificationReq {
        private String title;
        private String body;
        private UUID actorId;
    }

    @Data
    public static class ManualAuditReq {
        private String action;
        private Map<String, Object> before;
        private Map<String, Object> after;
        private UUID actorId;
    }
}
