// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\controller\WorkflowTaskController.java
package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.app.service.WorkflowTaskApplicationService;
import com.messdiener.cms.workflow.domain.dto.WorkflowTaskDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workflows/{workflowId}/tasks")
@Slf4j
public class WorkflowTaskController {

    private final WorkflowTaskApplicationService service;
    private final AuditService auditService;

    @GetMapping
    public List<WorkflowTaskDTO> list(@PathVariable UUID workflowId) {
        log.debug("[WF-TASK] list tasks for workflowId={}", workflowId);
        return service.listByWorkflow(workflowId);
    }

    @PostMapping
    public ResponseEntity<WorkflowTaskDTO> create(@PathVariable UUID workflowId, @RequestBody CreateTaskReq req) {
        log.info("[WF-TASK] create task for workflowId={}, title={}", workflowId, req.getTitle());
        var dto = service.createManualTask(
                workflowId,
                req.getTitle(),
                req.getState() == null ? CMSState.OPEN : req.getState(),
                req.getAssigneeId(),
                req.getDueAt(),
                req.getPayload()
        );
        auditService.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                workflowId,
                null,
                "Task created",
                "Task '" + dto.title() + "' angelegt"
        ));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("{taskId}/assign")
    public ResponseEntity<WorkflowTaskDTO> assign(@PathVariable UUID workflowId, @PathVariable UUID taskId, @RequestBody AssignReq req) {
        log.info("[WF-TASK] assign taskId={} to assigneeId={}", taskId, req.getAssigneeId());
        var dto = service.assign(taskId, req.getAssigneeId()).orElseThrow();
        auditService.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                workflowId,
                null,
                "Task assigned",
                "Task '" + dto.title() + "' zugewiesen"
        ));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("{taskId}/state")
    public ResponseEntity<WorkflowTaskDTO> setState(@PathVariable UUID workflowId, @PathVariable UUID taskId, @RequestBody StateReq req) {
        log.info("[WF-TASK] set state taskId={} => {}", taskId, req.getState());
        var dto = service.setState(taskId, req.getState()).orElseThrow();
        auditService.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                workflowId,
                null,
                "Task state",
                "Task '" + dto.title() + "' => " + dto.state()
        ));
        return ResponseEntity.ok(dto);
    }

    @Data
    public static class CreateTaskReq {
        private String title;
        private CMSState state;
        private UUID assigneeId;
        private CMSDate dueAt;
        private Map<String,Object> payload;
    }

    @Data
    public static class AssignReq { private UUID assigneeId; }

    @Data
    public static class StateReq { private CMSState state; }
}
