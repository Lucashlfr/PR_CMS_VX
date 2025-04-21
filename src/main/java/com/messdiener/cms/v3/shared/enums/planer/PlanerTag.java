package com.messdiener.cms.v3.shared.enums.planer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlanerTag {
    GLOBAL("Globale Informationen"),
    GENERAL("Allgemeine Informationen"),
    PRINCIPAL("Verantwortliche Personen"),
    LOCATION("Veranstaltungsort"),
    INSURANCE("Versicherung"),
    CONCEPT("Hygiene- und Präventionskonzept"),
    REGISTERED_PERSONS("Angemeldete Personen"),
    SCHEDULE("Zeitplan"),
    BUDGET("Budget und Ausgaben"),
    GRANTS("Zuschüsse"),
    REGISTRATION("Anmeldung"),
    PRESS_RELEASE("Pressemeldung"),
    EVENT("Übersicht"),
    PROCESS("Planungsverlauf");

    final String label;
}
