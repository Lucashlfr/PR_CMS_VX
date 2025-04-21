package com.messdiener.cms.v3.shared.enums.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskState {

    REDIRECTED("Weitergeleitet", 0, "bg-blue-soft", "text-blue"),
    PROGRESS("In Bearbeitung", 1, "bg-info-soft", "text-info"),
    WAITING("Warten", 2, "bg-yellow-soft", "text-yellow"),
    COMPLETED("Abgeschlossen",3, "bg-green-soft", "text-green"),
    REJECTED("Abgelehnt",3, "bg-red-soft", "text-red");

    private final String label;
    private final int kanban;
    private final String bg;
    private final String text;

}
