package com.messdiener.cms.v3.shared.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionState {

    CREATED("Erstellt", 0, true),
    BILL("Belege", 1, true),
    ACCOUNTING("Abrechnung", 2, true),
    TRANSFERRED("Pfarrbüro", 3, true),
    REVIEW("Prüfung", 4, true),
    COMPLETED("Abgeschlossen", 5, true),
    REJECTED("Abgelehnt", 98, false),
    ARCHIVED("Archiviert", 99, false);

    private final String label;
    private final int value;
    private final boolean active;

}
