package com.messdiener.cms.shared.enums.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventMessageType {

    INFO("Information", "IN", "text-info", "bg-info"),
    EXTERN("Externe Kommunikation", "EX", "text-pink", "bg-pink"),
    PREVENTION_CASE("Präventionsfall", "PV", "text-purple", "bg-purple"),
    VOTING("Abstimmung", "VT", "text-yellow", "bg-yellow"),
    DOCUMENTATION("Dokumentation", "DC", "bg-blue", "text-blue");

    private final String label;
    private final String code;
    private final String textColor;
    private final String bgColor;
}
