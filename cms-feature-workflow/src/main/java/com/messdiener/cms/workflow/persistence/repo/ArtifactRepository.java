// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\repo\ArtifactRepository.java
package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.ArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ArtifactRepository extends JpaRepository<ArtifactEntity, UUID> {
    List<ArtifactEntity> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
}
