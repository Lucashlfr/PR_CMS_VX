package com.messdiener.cms.shared.enums.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.flogger.Flogger;

@Getter
@AllArgsConstructor
public enum ElementType {

    ARTIFACT("Artifact", "bg-green"),
    AUDIT_TRAIL("Audit", "bg-dark"),
    AUTOMATION_RULE("Automatisierung", "bg-yellow"),
    DOCUMENTATION("Dokumentation", "bg-blue"),
    FORM_DEFINITION("Formular", "bg-info"),
    FORM_SUBMISSION("Formular", "bg-info"),
    NOTIFICATION("Notification", "bg-warning text-white"),
    TASK("Task", "bg-orange"),
    SLA("SLA", "bg-red");

    private final String label;
    private final String bgColor;

}
