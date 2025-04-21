package com.messdiener.cms.v3.shared.enums.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {

    START("bg-green", "flag", "text-green"),
    INFO("bg-info", "info", "text-info"),
    COMMENT( "bg-yellow", "message-circle", "text-yellow"),
    ENDE( "bg-success", "check-circle", "text-success"),
    EDIT("bg-orange", "edit", "text-orange"),
    REJECT("bg-red", "x-circle", "text-red"),;

    private final String color;
    private final String icon;
    private final String textColor;
}
