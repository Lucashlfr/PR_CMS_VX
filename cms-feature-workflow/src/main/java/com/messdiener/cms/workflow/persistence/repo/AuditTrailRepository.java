// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\repo\AuditTrailRepository.java
package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.AuditTrailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditTrailRepository extends JpaRepository<AuditTrailEntity, UUID> {
    List<AuditTrailEntity> findByWorkflowIdOrderByDateDesc(UUID workflowId);
}
