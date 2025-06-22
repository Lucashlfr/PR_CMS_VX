package com.messdiener.cms.v3.web.request;

import com.messdiener.cms.v3.shared.enums.event.EventState;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
public class EventForm {
    private UUID id;
    private String titel;
    private String beschreibung;
    private String eventType;
    private String targetgroup;
    private String eventState;
    private String startDate;
    private String endDate;
    private String deadline;
}
