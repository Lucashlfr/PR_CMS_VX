// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\repo\FormRepository.java
package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.persistence.entity.FormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormRepository extends JpaRepository<FormEntity, UUID> {

    List<FormEntity> findByWorkflowIdOrderByNumberAsc(UUID workflowId);

    Optional<FormEntity> findByWorkflowIdAndNumber(UUID workflowId, int number);

    int countByWorkflowId(UUID workflowId);

    @Query("select w.workflowId from FormEntity w where w.moduleId = :moduleId")
    Optional<UUID> findWorkflowIdByModuleId(@Param("moduleId") UUID moduleId);

    // FIX: richtige Tabelle
    @Query(value = """
            SELECT m.moduleId
            FROM module_workflow_forms m
            JOIN module_workflow w ON w.workflowId = m.workflowId
            WHERE m.number = w.currentNumber
              AND w.workflowId = :workflowId
            """, nativeQuery = true)
    Optional<UUID> findCurrentFormId(@Param("workflowId") UUID workflowId);

    // Wurde zuvor schon besprochen: ownerId -> currentId (oder ggf. creatorId)
    List<FormEntity> findByCurrentIdAndWorkflowIdAndStateInOrderByNumberAsc(
            UUID currentId, UUID workflowId, Collection<CMSState> states);

    List<FormEntity> findByCurrentIdAndStateInOrderByModificationDateDesc(
            UUID currentId, Collection<CMSState> states);

}
