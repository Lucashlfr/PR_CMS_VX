package com.messdiener.cms.v3.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LiturgieState {

    DUTY("Eingeteilt"),
    AVAILABLE("Verfügbar"),
    UNAVAILABLE("Nicht verfügbar");

    private final String label;

}
