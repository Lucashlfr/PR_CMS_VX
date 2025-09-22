package com.messdiener.cms.shared.enums.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkflowCategory {

    PVK("PVK", "Präventionskonzept", ""),
    ONB("ONB", "Onboarding", ""),
    DKM("DKM", "Dokumentation", "");

    private final String label;
    private final String description;
    private final String icon;
}
