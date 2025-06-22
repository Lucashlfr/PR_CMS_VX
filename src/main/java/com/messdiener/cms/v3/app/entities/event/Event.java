package com.messdiener.cms.v3.app.entities.event;

import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.event.EventState;
import com.messdiener.cms.v3.shared.enums.event.EventType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Event {

    // Identifikation
    private UUID eventId;
    private UUID tenantId;

    private int number;

    // Allgemeine Informationen
    private String title;
    private String description;
    private EventType type;
    private EventState state;

    // Zeitliche Angaben
    private CMSDate startDate;
    private CMSDate endDate;
    private CMSDate deadline;

    private CMSDate creationDate;
    private CMSDate resubmission;
    private CMSDate lastUpdate;

    private String schedule;
    private String registrationRelease;

    // Zielgruppe & Ort
    private String targetGroup;
    private String location;
    private String imgUrl;
    private int riskIndex;

    // Organisation & Verantwortliche
    private UUID currentEditor;
    private UUID createdBy;
    private UUID principal;
    private UUID manager;

    // Finanzen
    private double expenditure;
    private double revenue;

    // Presse & Dokumentation
    private String pressRelease;
    private String preventionConcept;
    private String notes;
    private String application;

    public static Event empty(UUID eventId, UUID tenantId, String title, EventType eventType, EventState eventState, CMSDate startDate, CMSDate endDate, CMSDate deadline) {
        return new Event(eventId, tenantId, 0, title, "", eventType, eventState, startDate, endDate, deadline, CMSDate.current(), CMSDate.current(), CMSDate.current(), "",
                "", "", "", "", -1, Cache.SYSTEM_USER, Cache.SYSTEM_USER, Cache.SYSTEM_USER, Cache.SYSTEM_USER, 0.0, 0.0,
                "", "", "", "");
    }

    public String getPressRelease() {
        return pressRelease.replace("<img src=", "<img class=\"img-fluid\" src=");
    }

}
