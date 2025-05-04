package com.messdiener.cms.v3.shared.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventState {

    CREATED("Erstellt", 0, "bg-blue-soft", "text-blue", "blue"),
    CONFIRMED("Bestätigt", 1,"text-green", "bg-green-soft", "green"),
    PLANNING("Planung", 2, "bg-info-soft", "text-info", "light-blue"),
    EXECUTION("Durchführung", 2, "bg-yellow-soft", "text-yellow", "orange"),
    FOLLOWUP("Nachbereitung", 2, "bg-yellow-soft", "text-yellow", "orange"),
    COMPLETED("Abgeschlossen",3, "bg-green-soft", "text-green", "green"),
    REJECTED("Verworfen",3, "bg-red-soft", "text-red", "red"),
    ARCHIVED("Archiviert", 4,"text-white", "bg-gray-600", "gray");

    private final String label;
    private final int id;
    private final String text;
    private final String bg;
    private final String color;


}
