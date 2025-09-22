// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\EventMessageRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.EventMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventMessageRepository extends JpaRepository<EventMessageEntity, UUID> {
    List<EventMessageEntity> findByEventIdOrderByDateAsc(UUID eventId);
    void deleteByMessageId(UUID messageId);
}
