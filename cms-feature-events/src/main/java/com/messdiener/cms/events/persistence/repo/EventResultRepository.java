// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\EventResultRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.EventResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventResultRepository extends JpaRepository<EventResultEntity, UUID> {
    List<EventResultEntity> findByEventIdOrderByDateDesc(UUID eventId);
}
