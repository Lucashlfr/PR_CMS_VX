// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\repo\WorkflowRepository.java
package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {

    List<WorkflowEntity> findByEditor(UUID editor);

    @Query(value = """
            SELECT
              w.workflowId,
              w.workflowType,
              w.workflowCategory,
              w.workflowState,
              w.workflowEditor,
              w.workflowCreator,
              w.workflowManager,
              w.workflowTarget,
              w.attachments,
              w.notes,
              w.creationDate,
              w.modificationDate,
              w.endDate,
              w.currentNumber,
              w.metaData
            FROM module_workflow w
            WHERE EXISTS (
              SELECT 1
              FROM module_person p
              WHERE p.person_id = w.workflowEditor
                AND p.tenant = :tenant
            )
            """, nativeQuery = true)
    List<WorkflowEntity> findAllByTenant(@Param("tenant") String tenant);

    // FIX: richtige Tabelle + richtige Spalte (currentId statt ownerId)
    @Query(value = """
            SELECT w.*
            FROM module_workflow w
            JOIN module_workflow_forms m ON m.workflowId = w.workflowId
            WHERE m.number = w.currentNumber
              AND m.currentId = :personId
              AND w.workflowState = 'ACTIVE'
            """, nativeQuery = true)
    List<WorkflowEntity> findRelevantByOwner(@Param("personId") String personId);

    @Query(value = """
            SELECT w.workflowState, COUNT(*) AS cnt
            FROM module_workflow w
            WHERE w.workflowEditor = :editor
            GROUP BY w.workflowState
            """, nativeQuery = true)
    List<Object[]> countStatesByEditor(@Param("editor") UUID editor);

    @Query(value = """
            SELECT w.workflowState, COUNT(*) AS cnt
            FROM module_workflow w
            WHERE EXISTS (
              SELECT 1
              FROM module_person p
              WHERE p.person_id = w.workflowEditor
                AND p.tenant = :tenant
            )
            GROUP BY w.workflowState
            """, nativeQuery = true)
    List<Object[]> countStatesByTenant(@Param("tenant") String tenant);
}
