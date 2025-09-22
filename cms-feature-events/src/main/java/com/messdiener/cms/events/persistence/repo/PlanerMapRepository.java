// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\PlanerMapRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.PlanerMapEntity;
import com.messdiener.cms.events.persistence.entity.id.PlanerMapId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanerMapRepository extends JpaRepository<PlanerMapEntity, PlanerMapId> {
    void deleteById_PlanerId(UUID planerId);
    List<PlanerMapEntity> findById_PlanerIdOrderById_UserIdAsc(UUID planerId);
    boolean existsById_PlanerIdAndId_UserId(UUID planerId, UUID userId);
}
