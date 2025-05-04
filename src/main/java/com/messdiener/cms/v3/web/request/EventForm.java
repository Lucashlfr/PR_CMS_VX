package com.messdiener.cms.v3.web.request;

import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
public class EventForm {
    private UUID id;
    private String titel;
    private String beschreibung;
    private String eventType;
    private String eventState;
    private String startDatum;
    private Optional<String> endDatum;

    private String targetgroup;
    private String manager;
    private String imgUrl;
}
