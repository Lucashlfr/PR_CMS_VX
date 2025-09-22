// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\repo\DocumentRepository.java
package com.messdiener.cms.documents.persistence.repo;

import com.messdiener.cms.documents.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    List<DocumentEntity> findByTarget(UUID target);
}
