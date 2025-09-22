// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\service\WorkflowAuditTrailService.java
package com.messdiener.cms.workflow.persistence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.workflow.persistence.entity.AuditTrailEntity;
import com.messdiener.cms.workflow.persistence.repo.AuditTrailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowAuditTrailService {

    private static final ObjectMapper M = new ObjectMapper();

    private final AuditTrailRepository repo;
    private final AuditService globalAudit;

    /**
     * Minimaler Delegator, damit Aufrufe wie
     * auditService.createLog(AuditLog.of(...)) kompilieren.
     * (Spiegelt bewusst NICHT automatisch in wf_audit_trails,
     * dafür gibt es logSimple(...).)
     */
    public void createLog(AuditLog log) {
        globalAudit.createLog(log);
    }

    /**
     * Schreibt ins lokale Workflow-Audit-Table und zusätzlich ins globale Audit.
     */
    public void logSimple(UUID workflowId,
                          UUID actorId,
                          String action,
                          Map<String, Object> before,
                          Map<String, Object> after) {
        AuditTrailEntity e = AuditTrailEntity.builder()
                .trailId(UUID.randomUUID())
                .workflowId(workflowId)
                .actorId(actorId)
                .action(action)
                .beforePayloadJson(toJson(before))
                .afterPayloadJson(toJson(after))
                .date(System.currentTimeMillis())
                .build();
        repo.save(e);

        globalAudit.createLog(AuditLog.of(
                MessageType.INFO,
                ActionCategory.WORKFLOW,
                workflowId,
                actorId,
                action,
                (after != null && !after.isEmpty()) ? after.toString() : ""
        ));
    }

    private static String toJson(Map<String, Object> map) {
        try { return map == null ? null : M.writeValueAsString(map); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
