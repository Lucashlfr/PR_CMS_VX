package com.messdiener.cms.v3.shared.enums;


import lombok.Getter;

public enum RequestState {

    NOT_SAVED("Nicht abgeschickt", "bg-gray-500", "text-dark", "bg-gray-100"),
    NOT_SEND("Nicht abgeschickt", "bg-gray-500", "text-dark", "bg-gray-100"),
    OPEN("In Bearbeitung", "bg-warning", "text-warning", "bg-warning-soft"),
    APPROVED("Genehmigt", "bg-info", "text-info", "bg-info-soft"),
    REJECTED("Abgelehnt", "bg-danger", "text-danger", "bg-danger-soft"),
    COMPLETED("Abgeschlossen", "bg-success", "text-success", "bg-success-soft");

    @Getter
    private final String text;

    @Getter
    private final String color;

    @Getter
    private final String tClass;

    @Getter
    private final String cColor;

    RequestState(String text, String color, String tClass, String cColor) {
        this.text = text;
        this.color = color;
        this.tClass = tClass;
        this.cColor = cColor;
    }

}
