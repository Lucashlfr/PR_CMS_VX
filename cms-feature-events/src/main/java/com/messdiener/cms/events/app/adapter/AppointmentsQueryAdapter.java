package com.messdiener.cms.events.app.adapter;

import com.messdiener.cms.domain.events.AppointmentItem;
import com.messdiener.cms.domain.events.AppointmentsQueryPort;
import com.messdiener.cms.events.domain.entity.Event;
import com.messdiener.cms.events.persistence.map.EventMapper;
import com.messdiener.cms.events.persistence.repo.EventRepository;
import com.messdiener.cms.shared.enums.event.EventState;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementierung des Domain-Ports für Termine auf Basis deines Event-Stacks.
 */
@Service
@RequiredArgsConstructor
public class AppointmentsQueryAdapter implements AppointmentsQueryPort {

    private final EventRepository eventRepository;

    @Override
    public List<AppointmentItem> findUpcoming(int limit) {
        if (limit <= 0) limit = 10;

        long now = System.currentTimeMillis();
        // naive Strategie: alle Events mit Startdatum in der Zukunft, sortiert aufsteigend
        return eventRepository.findAll().stream()
                .map(EventMapper::toDomain)
                .filter(ev -> ev.getStartDate() != null && ev.getStartDate().toLong() >= now)
                .sorted(Comparator.comparing(ev -> ev.getStartDate().toLong()))
                .limit(limit)
                .map(this::toItem)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentItem> findAll() {
        return eventRepository.findAll().stream()
                .map(com.messdiener.cms.events.persistence.map.EventMapper::toDomain)
                .sorted(Comparator.comparing(ev -> ev.getStartDate() == null ? Long.MAX_VALUE : ev.getStartDate().toLong()))
                .map(this::toItem)
                .collect(Collectors.toList());
    }

    private AppointmentItem toItem(Event ev) {
        String title = safe(ev.getTitle());
        String date = ev.getStartDate() == null ? "" : germanDate(ev.getStartDate());
        String location = safe(ev.getLocation());
        String status = mapStatus(ev.getState());
        String imageUrl = safe(ev.getImgUrl());

        return new AppointmentItem(title, date, location, status, imageUrl);
    }

    private static String germanDate(CMSDate d) {
        // Frontend-Beispiel nutzt "dd.MM.yyyy" – dein CMSDate kann das (vgl. andere Verwendungen)
        return d.getGermanDate();
    }

    private static String mapStatus(EventState s) {
        if (s == null) return "open";
        return switch (s) {
            case CONFIRMED -> "confirmed";
            case CANCELED -> "canceled";
            default -> "open";
        };
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
