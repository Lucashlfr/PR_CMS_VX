// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\PlanerTaskRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.PlanerTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanerTaskRepository extends JpaRepository<PlanerTaskEntity, UUID> {
    List<PlanerTaskEntity> findByPlanerIdOrderByTaskNumberAsc(UUID planerId);
}
