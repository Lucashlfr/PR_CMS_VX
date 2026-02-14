// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\app\service\WorkflowService.java
package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import com.messdiener.cms.workflow.persistence.entity.WorkflowEntity;
import com.messdiener.cms.workflow.persistence.map.WorkflowMapper;
import com.messdiener.cms.workflow.persistence.repo.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);
    private final WorkflowRepository workflowRepository;

    // --- Public API (Signaturen bleiben unverändert zu den Call-Sites) ---

    public Optional<Workflow> getWorkflowById(UUID workflowId) {
        return workflowRepository.findById(workflowId).map(WorkflowMapper::toDomain); // :contentReference[oaicite:20]{index=20}
    }

    public List<Workflow> getWorkflowsByUserId(UUID workflowEditor) {
        return workflowRepository.findByEditor(workflowEditor)
                .stream().map(WorkflowMapper::toDomain).toList(); // :contentReference[oaicite:21]{index=21}
    }

    public List<Workflow> getAllWorkflowsByTenant(Tenant tenant) {
        return workflowRepository.findAllByTenant(tenant.toString())
                .stream().map(WorkflowMapper::toDomain).toList(); // :contentReference[oaicite:22]{index=22}
    }

    public void saveWorkflow(Workflow workflow) {
        WorkflowEntity entity = WorkflowMapper.toEntity(workflow);
        // fehlende Timestamps robust setzen
        if (entity.getCreationDate() == null) entity.setCreationDate(System.currentTimeMillis());
        if (entity.getModificationDate() == null) entity.setModificationDate(entity.getCreationDate());
        workflowRepository.save(entity); // ersetzt INSERT ... ON DUPLICATE KEY UPDATE :contentReference[oaicite:23]{index=23}
        LOGGER.info("Saved workflow with ID {}", workflow.getWorkflowId());
    }

    public List<Workflow> getRelevantWorkflows(String userId) {
        List<WorkflowEntity> rows = workflowRepository.findRelevantByOwner(userId); // Fix gegenüber Alt-SQL (ownerId statt "workflowEditor") :contentReference[oaicite:24]{index=24}
        return rows.stream().map(WorkflowMapper::toDomain).toList();
    }

    public Map<String, Integer> countWorkflowStates(UUID userId) {
        Map<String, Integer> map = new HashMap<>();
        for (Object[] row : workflowRepository.countStatesByEditor(userId)) { // :contentReference[oaicite:25]{index=25}
            String state = Objects.toString(row[0], CMSState.ACTIVE.name());
            Number cnt = (Number) row[1];
            map.put(state, cnt != null ? cnt.intValue() : 0);
        }
        return map;
    }

    public Map<String, Integer> countWorkflowStatesByTenant(Tenant tenant) {
        Map<String, Integer> map = new HashMap<>();
        for (Object[] row : workflowRepository.countStatesByTenant(tenant.toString())) { // :contentReference[oaicite:26]{index=26}
            String state = Objects.toString(row[0], CMSState.ACTIVE.name());
            Number cnt = (Number) row[1];
            map.put(state, cnt != null ? cnt.intValue() : 0);
        }
        return map;
    }
}
