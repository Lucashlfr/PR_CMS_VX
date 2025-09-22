package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.WorkflowTaskDTO;
import com.messdiener.cms.workflow.persistence.entity.WorkflowTaskEntity;
import com.messdiener.cms.workflow.persistence.map.WorkflowTaskMapper;
import com.messdiener.cms.workflow.persistence.repo.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowTaskService {

    private final WorkflowTaskRepository repo;
    private final WorkflowAuditTrailService audit;

    public WorkflowTaskDTO save(WorkflowTaskDTO dto) {
        WorkflowTaskEntity saved = repo.save(WorkflowTaskMapper.toEntity(dto));
        return WorkflowTaskMapper.toDto(saved);
    }

    public Optional<WorkflowTaskDTO> getById(UUID id) {
        return repo.findById(id).map(WorkflowTaskMapper::toDto);
    }

    public List<WorkflowTaskDTO> getByWorkflow(UUID workflowId) {
        return repo.findByWorkflowIdOrderByCreatedAtDesc(workflowId).stream()
                .map(WorkflowTaskMapper::toDto).toList();
    }

    public List<WorkflowTaskDTO> getMyOpen(UUID assigneeId) {
        return repo.findByAssigneeIdAndStateOrderByCreatedAtDesc(assigneeId, CMSState.OPEN)
                .stream().map(WorkflowTaskMapper::toDto).toList();
    }

    /* Use Cases */

    public WorkflowTaskDTO createManualTask(UUID workflowId,
                                            String key,
                                            String title,
                                            UUID creatorId,
                                            UUID assigneeId,
                                            List<String> candidateRoles,
                                            CMSDate dueAt,
                                            Map<String, Object> payload) {
        WorkflowTaskDTO dto = new WorkflowTaskDTO(
                UUID.randomUUID(),
                workflowId,
                key,
                title,
                CMSState.OPEN,
                candidateRoles == null ? List.of() : candidateRoles,
                assigneeId,
                dueAt,
                CMSDate.current(),
                payload == null ? Map.of() : payload
        );
        WorkflowTaskDTO saved = save(dto);
        audit.logSimple(workflowId, creatorId, "TASK_CREATED",
                Map.of("taskId", saved.id().toString(), "title", saved.title()), null);
        return saved;
    }

    public WorkflowTaskDTO assign(UUID taskId, UUID actorId, UUID newAssignee) {
        WorkflowTaskEntity e = repo.findById(taskId).orElseThrow();
        e.setAssigneeId(newAssignee);
        WorkflowTaskDTO updated = WorkflowTaskMapper.toDto(repo.save(e));
        audit.logSimple(e.getWorkflowId(), actorId, "TASK_ASSIGNED",
                Map.of("taskId", taskId.toString(), "assigneeId", String.valueOf(newAssignee)), null);
        return updated;
    }

    public WorkflowTaskDTO start(UUID taskId, UUID actorId) {
        WorkflowTaskEntity e = repo.findById(taskId).orElseThrow();
        if (e.getState() == CMSState.PROGRESS) return WorkflowTaskMapper.toDto(e);
        e.setState(CMSState.PROGRESS);
        WorkflowTaskDTO updated = WorkflowTaskMapper.toDto(repo.save(e));
        audit.logSimple(e.getWorkflowId(), actorId, "TASK_STARTED", Map.of("taskId", taskId.toString()), null);
        return updated;
    }

    public WorkflowTaskDTO complete(UUID taskId, UUID actorId, Map<String, Object> resultPayload) {
        WorkflowTaskEntity e = repo.findById(taskId).orElseThrow();
        e.setState(CMSState.COMPLETED);
        // Payload mergen:
        Map<String,Object> merged = new LinkedHashMap<>();
        if (e.getPayloadJson() != null) merged.putAll(com.messdiener.cms.workflow.persistence.map.Jsons.toMap(e.getPayloadJson()));
        if (resultPayload != null) merged.putAll(resultPayload);
        e.setPayloadJson(com.messdiener.cms.workflow.persistence.map.Jsons.toJson(merged));
        WorkflowTaskDTO updated = WorkflowTaskMapper.toDto(repo.save(e));
        audit.logSimple(e.getWorkflowId(), actorId, "TASK_COMPLETED", Map.of("taskId", taskId.toString()), resultPayload);
        return updated;
    }

    public WorkflowTaskDTO reject(UUID taskId, UUID actorId, String reason) {
        WorkflowTaskEntity e = repo.findById(taskId).orElseThrow();
        e.setState(CMSState.REJECTED);
        WorkflowTaskDTO updated = WorkflowTaskMapper.toDto(repo.save(e));
        audit.logSimple(e.getWorkflowId(), actorId, "TASK_REJECTED",
                Map.of("taskId", taskId.toString(), "reason", reason), null);
        return updated;
    }
}
