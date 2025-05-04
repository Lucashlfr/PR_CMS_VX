package com.messdiener.cms.v3.shared.enums;

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
