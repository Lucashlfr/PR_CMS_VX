package com.messdiener.cms.v3.shared.enums;

import lombok.Getter;

public class Finance {

    @Getter
    public enum RequestState {

        NOT_SAVED("Nicht abgeschickt", "bg-gray-500", "text-dark", "bg-gray-100", 1),
        NOT_SEND("Nicht abgeschickt", "bg-gray-500", "text-dark", "bg-gray-100", 2),
        OPEN("In Bearbeitung", "bg-warning", "text-warning", "bg-warning-soft", 4),
        APPROVED("Genehmigt", "bg-info", "text-info", "bg-info-soft", 5),
        REJECTED("Abgelehnt", "bg-danger", "text-danger", "bg-danger-soft", 8),
        COMPLETED("Abgeschlossen", "bg-success", "text-success", "bg-success-soft", 9);

        private final String text;

        private final String color;

        private final String tClass;

        private final String cColor;

        private final int codeNr;

        RequestState(String text, String color, String tClass, String cColor, int codeNr) {
            this.text = text;
            this.color = color;
            this.tClass = tClass;
            this.cColor = cColor;
            this.codeNr = codeNr;
        }

    }

    @Getter
    public enum DocumentType {

        EXPENSE("Ausgabe"), REVENUE("Einnahme"), NOT_SELECTED("");

        private final String text;

        DocumentType(String text) {
            this.text = text;
        }
    }

    @Getter
    public enum CashRegisterType {

        CASH("Barkasse"), ACCOUNT("Konto"), EXCHANGE("Wechselgeld"), NOT_SELECTED("");

        private final String text;

        CashRegisterType(String text) {
            this.text = text;
        }
    }

}
