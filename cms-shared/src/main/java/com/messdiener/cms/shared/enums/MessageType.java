package com.messdiener.cms.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {

    START("Start", "bg-green-soft text-green", "flag"),
    INFO("Info", "bg-info-soft text-info", "info"),
    COMMENT("Kommentar", "bg-yellow-soft text-yellow", "message-circle"),
    ENDE("Ende", "bg-success-soft text-success", "check-circle"),
    EDIT("Bearbeitet", "bg-orange-soft text-orange", "edit"),
    REJECT("Abgelehnt", "bg-red-soft text-red", "x-circle"),

    STEP_STARTED("Schritt gestartet", "bg-green-soft text-green", "flag"),
    STEP_COMPLETED("Schritt abgeschlossen", "bg-success-soft text-success", "check-circle"),
    STEP_REJECTED("Schritt abgelehnt", "bg-red-soft text-red", "x-circle"),
    STEP_SKIPPED("Schritt übersprungen", "bg-blue-soft text-blue", "x-circle"),
    WORKFLOW_COMPLETED("Workflow abgeschlossen", "bg-success-soft text-success", "check-circle"),
    WORKFLOW_CANCELLED("Workflow abgebrochen", "bg-red-soft text-red", "x-circle"),
    ROLLBACK("Rollback", "bg-yellow-soft text-yellow", "message-circle"),
    ATTACHMENT_UPLOADED("Anhang hochgeladen", "bg-yellow-soft text-yellow", "upload"),
    CREATED("Erstellt", "bg-yellow-soft text-yellow", "info"),

    NULL("", "", "");

    private final String label;
    private final String color; // enthält bg-... und text-...
    private final String icon;
}
