// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\PlanerPrincipalRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.PlanerPrincipalEntity;
import com.messdiener.cms.events.persistence.entity.id.PlanerPrincipalId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanerPrincipalRepository extends JpaRepository<PlanerPrincipalEntity, PlanerPrincipalId> {
    void deleteById_PlanerId(UUID planerId);
    List<PlanerPrincipalEntity> findById_PlanerId(UUID planerId);
}
