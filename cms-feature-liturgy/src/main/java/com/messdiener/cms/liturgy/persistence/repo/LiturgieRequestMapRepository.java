// src/main/java/com/messdiener/cms/liturgy/persistence/repo/LiturgieRequestMapRepository.java
package com.messdiener.cms.liturgy.persistence.repo;

import com.messdiener.cms.liturgy.persistence.entity.LiturgieRequestMapEntity;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieRequestMapId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LiturgieRequestMapRepository extends JpaRepository<LiturgieRequestMapEntity, LiturgieRequestMapId> {

    boolean existsByIdRequestIdAndIdPersonId(UUID requestId, UUID personId);
}
