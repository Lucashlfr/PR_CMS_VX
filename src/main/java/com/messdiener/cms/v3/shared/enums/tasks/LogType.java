package com.messdiener.cms.v3.shared.enums.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogType {

    INFO("bg-info", "info", "text-info"),
    COMMENT( "bg-yellow", "flag", "text-yellow"),
    ENDE( "bg-primary", "map-pin", "text-primary"),
    EDIT("bg-orange", "edit", "text-orange"),
    REJECT("bg-red", "x-circle", "text-red"),;

    private final String color;
    private final String icon;
    private final String textColor;
}
