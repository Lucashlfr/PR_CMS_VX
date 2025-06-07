package com.messdiener.cms.v3.shared.enums.document;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {

    ONBOARDING("Onboarding"), TRAINING("Schulungen"), ACCOUNTING("Abrechnung"), OTHER("Sonstiges"), NULL("Kein Typ"), EVENT("Event"), WEBSITE("Website"), TRANSACTION("Beleg zur Transaktion");

    private final String label;

}
