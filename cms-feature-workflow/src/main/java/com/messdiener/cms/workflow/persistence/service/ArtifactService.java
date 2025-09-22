// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\service\ArtifactService.java
package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.workflow.persistence.entity.ArtifactEntity;
import com.messdiener.cms.workflow.persistence.repo.ArtifactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtifactService {

    private final ArtifactRepository repo;
    private final WorkflowAuditTrailService audit;

    public ArtifactEntity addDocumentArtifact(UUID workflowId, UUID actorId) {
        ArtifactEntity e = ArtifactEntity.builder()
                .artifactId(UUID.randomUUID())
                .workflowId(workflowId)
                .artifactType("DOCUMENT")
                .createdAt(System.currentTimeMillis())
                .createdBy(actorId)
                .build();
        ArtifactEntity saved = repo.save(e);

        audit.logSimple(workflowId, actorId, "DOCUMENT_UPLOADED",
                null, java.util.Map.of("artifactId", saved.getArtifactId().toString()));

        return saved;
    }

    public List<ArtifactEntity> getByWorkflow(UUID workflowId) {
        return repo.findByWorkflowIdOrderByCreatedAtDesc(workflowId);
    }
}
