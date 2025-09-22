package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.DocumentationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentationRepository extends JpaRepository<DocumentationEntity, UUID> {
    List<DocumentationEntity> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
}
