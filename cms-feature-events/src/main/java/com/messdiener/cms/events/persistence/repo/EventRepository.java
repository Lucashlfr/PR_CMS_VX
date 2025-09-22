// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\EventRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    List<EventEntity> findByTenant(String tenant);
    List<EventEntity> findByDeadlineGreaterThan(Long epochMs);
    List<EventEntity> findByEventStateInOrderByEndDateDesc(List<String> states);
}
