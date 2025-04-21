package com.messdiener.cms.v3.shared.enums.tasks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskPriority {
    NULL("Keine"), P1("Prio 1");

    private final String label;
}
