package com.messdiener.cms.v3.shared.enums;

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
    REJECT("bg-red", "x-circle", "text-red"),

    STEP_STARTED("bg-green", "flag", "text-green"),
    STEP_COMPLETED ("bg-success", "check-circle", "text-success"),
    STEP_REJECTED("bg-red", "x-circle", "text-red"),
    STEP_SKIPPED("bg-blue", "x-circle", "text-blue"),
    WORKFLOW_COMPLETED ("bg-success", "check-circle", "text-success"),
    WORKFLOW_CANCELLED("bg-red", "x-circle", "text-red"),
    ROLLBACK( "bg-yellow", "message-circle", "text-yellow"),
    ATTACHMENT_UPLOADED( "bg-yellow", "upload", "text-yellow");

    private final String color;
    private final String icon;
    private final String textColor;
}
