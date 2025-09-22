// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\app\service\EventMessageService.java
package com.messdiener.cms.events.app.service;

import com.messdiener.cms.events.domain.entity.data.MessageItem;
import com.messdiener.cms.events.persistence.map.EventMessageMapper;
import com.messdiener.cms.events.persistence.repo.EventMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventMessageService {

    private final EventMessageRepository repo;

    public void createMessage(UUID eventId, MessageItem messageItem) {
        // Semantik: zuvor delete-by-messageId + insert → save() via @Id (messageId)
        repo.save(EventMessageMapper.toEntity(eventId, messageItem));
    }

    public List<MessageItem> getItems(UUID eventId) {
        return repo.findByEventIdOrderByDateAsc(eventId)
                .stream().map(EventMessageMapper::toDomain).collect(Collectors.toList());
    }
}
