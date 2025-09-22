package com.messdiener.cms.shared.enums.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkflowModuleStatus {

    OPEN("Offen"),
    IN_PROGRESS("In Bearbeitung"),
    COMPLETED("Abgeschlossen"),
    SKIPPED("Übersprungen"),
    REJECTED("Abgelehnt"),
    WAITING("Wartend");

    private final String lable;

}
