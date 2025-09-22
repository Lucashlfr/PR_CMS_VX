// src/main/java/com/messdiener/cms/liturgy/persistence/repo/LiturgieRequestRepository.java
package com.messdiener.cms.liturgy.persistence.repo;

import com.messdiener.cms.liturgy.persistence.entity.LiturgieRequestEntity;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiturgieRequestRepository extends JpaRepository<LiturgieRequestEntity, UUID> {

    List<LiturgieRequestEntity> findByTenantOrderByNumberDesc(Tenant tenant);

    Optional<LiturgieRequestEntity> findTopByTenantAndActiveTrueOrderByNumberDesc(Tenant tenant);
}
