package com.messdiener.cms.v3.shared.enums.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventType {

    SMALL_ACTION("Aktion"), BIG_ACTION("Aktion (mit Übernachtung)"), GROUP_SESSION("Gruppenstunde"), MEETING("Meeting"), OTHER("Sonstiges");

    private final String label;

}
