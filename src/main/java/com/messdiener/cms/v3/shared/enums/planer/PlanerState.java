package com.messdiener.cms.v3.shared.enums.planer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlanerState {

    CREATED("Erstellt", 0, "bg-blue-soft", "text-blue"),
    PLANING("Planung", 1, "bg-info-soft", "text-info"),
    EXECUTION("Durchführung", 2, "bg-yellow-soft", "text-yellow"),
    FOLLOWUP("Nachbereitung", 2, "bg-yellow-soft", "text-yellow"),
    COMPLETED("Abgeschlossen",3, "bg-green-soft", "text-green"),
    REJECTED("Verworfen",3, "bg-red-soft", "text-red");

    private final String label;
    private final int kanban;
    private final String bg;
    private final String text;

}
