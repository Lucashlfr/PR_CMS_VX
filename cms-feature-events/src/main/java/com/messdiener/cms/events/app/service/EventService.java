// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\app\service\EventService.java
package com.messdiener.cms.events.app.service;

import com.messdiener.cms.domain.document.ArticleView;
import com.messdiener.cms.events.app.utils.SlugUtil;
import com.messdiener.cms.events.domain.entity.Event;
import com.messdiener.cms.events.persistence.map.EventMapper;
import com.messdiener.cms.events.persistence.repo.EventRepository;
import com.messdiener.cms.shared.enums.event.EventState;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repo;

    public List<Event> getEvents() {
        return repo.findAll().stream().map(EventMapper::toDomain).collect(Collectors.toList());
    }

    public Optional<Event> getEventById(UUID eventId) {
        return repo.findById(eventId).map(EventMapper::toDomain);
    }

    public List<Event> getEventsByTenant(Tenant tenant) {
        return repo.findByTenant(tenant.toString()).stream().map(EventMapper::toDomain).collect(Collectors.toList());
    }

    public List<Event> getEventsAtDeadline() {
        return repo.findByDeadlineGreaterThan(System.currentTimeMillis()).stream().map(EventMapper::toDomain).collect(Collectors.toList());
    }

    public List<Event> getEventsForState() {
        List<String> states = Arrays.asList(EventState.CONFIRMED.toString(), EventState.COMPLETED.toString());
        return repo.findByEventStateInOrderByEndDateDesc(states).stream().map(EventMapper::toDomain).collect(Collectors.toList());
    }

    // <— fehlt im Controller-Aufruf: save(Event)  —>
    public void save(Event event) {
        repo.save(EventMapper.toEntity(event));
    }

    public Optional<Event> getArticleBySlug(String slug) {

        return repo.findAll().stream()
                .filter(e -> SlugUtil.toSlug(e.getTitle().toLowerCase()).equals(slug))
                .findFirst().map(EventMapper::toDomain);
    }
}
