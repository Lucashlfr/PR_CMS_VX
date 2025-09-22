package com.messdiener.cms.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionCategory {
    NULL("Keine"),
    ONBOARDING("Onboarding"),
    WORKFLOW("Workflows"),
    PERSON("Person"),
    EVENT("Event"),
    FINANCE("Finanzen"),;

    private final String label;
}
