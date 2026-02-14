package com.messdiener.cms.shared.enums.document;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {

    ONBOARDING("Onboarding"), TRAINING("Schulungen"), ACCOUNTING("Abrechnung"), OTHER("Sonstiges"), NULL("Kein Typ"), EVENT("Event"), WEBSITE("Website"), TRANSACTION("Beleg zur Transaktion"), PERSONAL("Personal");

    private final String label;

}
