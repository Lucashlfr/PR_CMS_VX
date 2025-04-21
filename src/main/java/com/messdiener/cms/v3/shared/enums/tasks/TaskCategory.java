package com.messdiener.cms.v3.shared.enums.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskCategory {
    NULL("Keine"), ONBOARDING("Onboarding"), WORKFLOW("Workflows");

    private final String label;
}
