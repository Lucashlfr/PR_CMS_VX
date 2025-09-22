package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.persistence.entity.WorkflowTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTaskEntity, UUID> {
    List<WorkflowTaskEntity> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
    List<WorkflowTaskEntity> findByAssigneeIdAndStateOrderByCreatedAtDesc(UUID assigneeId, CMSState state);
}
