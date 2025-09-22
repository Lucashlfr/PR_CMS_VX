// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\EventComponentRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.EventComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventComponentRepository extends JpaRepository<EventComponentEntity, Long> {
    List<EventComponentEntity> findByEventIdOrderByNumberAsc(UUID eventId);
    Optional<EventComponentEntity> findByEventIdAndNumber(UUID eventId, Integer number);
    void deleteByEventIdAndNumber(UUID eventId, Integer number);
}
