// src/main/java/com/messdiener/cms/workflow/persistence/repo/FormDefinitionRepository.java
package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.FormDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FormDefinitionRepository extends JpaRepository<FormDefinitionEntity, UUID> {

    Optional<FormDefinitionEntity> findFirstByKeyAndVersion(String key, Integer version);
}
