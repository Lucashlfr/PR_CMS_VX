package com.messdiener.cms.shared.enums.workflow;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CMSState {

    ACTIVE("Aktiv", "bg-blue-soft text-blue"),
    CANCELLED("Abgebrochen", "bg-red-soft text-red"),
    SUSPENDED("Ausgesetzt", "bg-red-soft text-red"),

    REDIRECTED("Weitergeleitet", "bg-blue-soft text-blue"),
    PROGRESS("In Bearbeitung", "bg-info-soft text-info"),
    WAITING("Warten", "bg-yellow-soft text-yellow"),
    COMPLETED("Abgeschlossen","bg-green-soft text-green"),
    REJECTED("Abgelehnt", "bg-red-soft text-red"),
    OPEN("Offen", "bg-yellow-soft text-yellow" ),
    INFO("Information","bg-info-soft text-info"),
    SKIPPED("Übersprungen", "bg-yellow-soft text-yellow"),
    NULL("","" );

    private final String label;
    private final String color;

    public String getBgColor(){
        return (color.split(" ").length > 0) ? color.split(" ")[0] : "";
    }

}
