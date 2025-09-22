// src/main/java/com/messdiener/cms/liturgy/persistence/repo/LiturgieRepository.java
package com.messdiener.cms.liturgy.persistence.repo;

import com.messdiener.cms.liturgy.persistence.entity.LiturgieEntity;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface LiturgieRepository extends JpaRepository<LiturgieEntity, UUID> {

    List<LiturgieEntity> findByTenantAndDateBetweenOrderByDateAsc(Tenant tenant, long start, long end);

    List<LiturgieEntity> findByTenantAndDateGreaterThanEqualOrderByDateAsc(Tenant tenant, long from);

    // NEU: wird in LiturgieService.findByIdsFromDate(...) verwendet
    List<LiturgieEntity> findByIdInAndDateGreaterThanEqualOrderByDateAsc(Set<UUID> ids, long from);

    @Query("select coalesce(max(l.number), 0) from LiturgieEntity l where l.tenant = :tenant")
    int maxNumberByTenant(@Param("tenant") Tenant tenant);
}
