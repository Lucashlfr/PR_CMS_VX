package com.messdiener.cms.v3.shared.enums.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskType {

    INFO("Information", "bg-info", "info", "text-info"),
    APPROVAL("Freigabe", "bg-green", "flag", "text-green"),
    JOB("Aufgabe", "bg-warning", "map-pin", "text-warning"),
    INPUT("Eingabe", "bg-pink", "git-commit", "text-pink"),
    DOCUMENTATION("Dokumentation", "bg-blue", "archive", "text-blue"),;

    private final String label;
    private final String color;
    private final String icon;
    private final String textColor;
}
