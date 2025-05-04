package com.messdiener.cms.v3.shared.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CostCenter {

    BUSINESS_EXPENSES("Geschäftskosten"),
    MEASURES("Maßnahmen"),
    TRAININGS("Schulungen"),
    LEADS("Obermessdiener"),
    TEAMER("Leitungsteam"),
    MEETINGS("Versammlungen"),
    SESSIONS("Gruppenstunden"),
    MANAGEMENT_OFFICE("Leitungsbüro"),
    ADVERTISING_COSTS("Werbekosten"),
    OTHER("Sonstiges"),
    NULL("");

    private final String label;
}
