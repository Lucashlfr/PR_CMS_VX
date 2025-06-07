package com.messdiener.cms.v3.shared.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventState {

    CREATED("Erstellt", "bg-cyan-soft", "text-cyan", "DarkTurquoise"),
    PLANNING("Planung", "bg-blue-soft", "text-blue", "RoyalBlue"),
    CONFIRMED("Bestätigt","bg-teal-soft", "text-teal", "teal"),
    EXECUTION("Durchführung",  "bg-yellow-soft", "text-yellow", "Gold"),
    FOLLOWUP("Nachbereitung",  "bg-orange-soft", "text-orange", "Orange"),
    COMPLETED("Abgeschlossen", "bg-green-soft", "text-green", "YellowGreen"),
    REJECTED("Verworfen", "bg-red-soft", "text-red", "Tomato"),
    ARCHIVED("Archiviert","bg-gray-600", "text-white", "gray");

    private final String label;
    private final String background;
    private final String textColor;
    private final String calendarColor;

    public String getColors(){
        return background + " " + textColor;
    }
}
