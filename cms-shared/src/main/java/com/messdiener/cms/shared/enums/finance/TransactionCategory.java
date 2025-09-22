package com.messdiener.cms.shared.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionCategory {
    NULL("Kein Budget Ausgewählt"),
    BUSINESS_EXPENSES("Geschäftskosten"),
    PROFESSIONAL_LITERATURE("Fachliteratur"),
    EVENT_Q1("Monatsaktion Quartal 1"),
    EVENT_Q2("Monatsaktion Quartal 2"),
    EVENT_Q3("Monatsaktion Quartal 3"),
    EVENT_Q4("Monatsaktion Quartal 4"),
    OTHER_EVENTS("Sonstige Aktionen"),
    GROUP_SESSIONS("Gruppenstunden"),
    TRAININGS("Schulungen"),
    LEADERSHIP_TEAM("Leitungsteam"),
    GROUP_LEADERSHIP("Gruppenleitung"),
    ADVERTISING_COSTS("Werbekosten"),
    PASS_THROUGH_FUNDS("Durchlaufende Gelder"),
    SUBSIDIES("Zuschüsse"),
    MISC("Sonstiges");

    private final String label;
}

