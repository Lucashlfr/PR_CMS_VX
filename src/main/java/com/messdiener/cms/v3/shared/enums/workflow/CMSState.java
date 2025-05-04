package com.messdiener.cms.v3.shared.enums.workflow;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CMSState {

    ACTIVE("Aktiv", "bg-blue-soft", "text-blue"),
    CANCELLED("Abgebrochen", "bg-red-soft", "text-red"),
    SUSPENDED("Ausgesetzt", "bg-red-soft", "text-red"),

    REDIRECTED("Weitergeleitet", "bg-blue-soft", "text-blue"),
    PROGRESS("In Bearbeitung", "bg-info-soft", "text-info"),
    WAITING("Warten", "bg-yellow-soft", "text-yellow"),
    COMPLETED("Abgeschlossen","bg-green-soft", "text-green"),
    REJECTED("Abgelehnt", "bg-red-soft", "text-red");

    private final String label;
    private final String bg;
    private final String text;


}
