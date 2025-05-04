package com.messdiener.cms.v3.app.entities.event;

import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.event.EventState;
import com.messdiener.cms.v3.shared.enums.event.EventType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Event {

    // Identifikation
    private UUID eventId;
    private UUID tenantId;

    private UUID updatedBy;
    private CMSDate updatedDate;

    // Allgemeine Informationen
    private String title;
    private String description;
    private EventType type;
    private EventState state;

    // Zeitliche Angaben
    private CMSDate startDate;
    private Optional<CMSDate> endDate;
    private String schedule;
    private String registrationRelease;

    // Zielgruppe & Ort
    private String targetGroup;
    private String location;
    private String imgUrl;
    private int rinkIndex;

    // Organisation & Verantwortliche
    private UUID managerId;
    private List<UUID> principals;

    // Finanzen
    private double expenditure;
    private double revenue;

    // Presse & Dokumentation
    private String pressRelease;
    private String preventionConcept;

    public static Event empty(UUID eventId, UUID tenantId, String title, EventType eventType, EventState eventState, CMSDate startDate, Optional<CMSDate> endDate) {
        return new Event(eventId, tenantId, Cache.SYSTEM_USER, CMSDate.current(), title, "", eventType, eventState, startDate, endDate, "", "", "",
                "", "", -1, Cache.SYSTEM_USER, new ArrayList<>(), 0, 0, "", "");

    }

}
