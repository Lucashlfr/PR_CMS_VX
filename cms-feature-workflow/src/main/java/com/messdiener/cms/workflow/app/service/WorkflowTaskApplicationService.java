package com.messdiener.cms.workflow.app.service;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.WorkflowTaskDTO;
import com.messdiener.cms.workflow.persistence.map.WorkflowTaskMapper;
import com.messdiener.cms.workflow.persistence.repo.WorkflowTaskRepository;
import com.messdiener.cms.workflow.persistence.entity.WorkflowTaskEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowTaskApplicationService {

    private final WorkflowTaskRepository repo;

    public List<WorkflowTaskDTO> listByWorkflow(UUID workflowId) {
        return repo.findByWorkflowIdOrderByCreatedAtDesc(workflowId)
                .stream().map(WorkflowTaskMapper::toDto).collect(Collectors.toList());
    }

    public WorkflowTaskDTO createManualTask(UUID workflowId,
                                            String title,
                                            CMSState state,
                                            UUID assigneeId,
                                            CMSDate dueAt,
                                            Map<String,Object> payload) {

        WorkflowTaskDTO dto = new WorkflowTaskDTO(
                UUID.randomUUID(),
                workflowId,
                null,
                title,
                state == null ? CMSState.OPEN : state,
                List.of(),
                assigneeId,
                dueAt,
                CMSDate.current(),
                payload == null ? Map.of() : payload
        );
        var saved = repo.save(WorkflowTaskMapper.toEntity(dto));
        return WorkflowTaskMapper.toDto(saved);
    }

    public Optional<WorkflowTaskDTO> assign(UUID taskId, UUID assigneeId) {
        return repo.findById(taskId).map(e -> {
            e.setAssigneeId(assigneeId);
            return WorkflowTaskMapper.toDto(repo.save(e));
        });
    }

    public Optional<WorkflowTaskDTO> setState(UUID taskId, CMSState state) {
        return repo.findById(taskId).map(e -> {
            e.setState(state);
            return WorkflowTaskMapper.toDto(repo.save(e));
        });
    }
}
