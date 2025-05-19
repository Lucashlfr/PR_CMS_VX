package com.messdiener.cms.v3.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LiturgieType {

    EVE_MASS("Vorabendgottesdienst"),
    SUNDAY_SERVICE("Sonntagsgottestienst"),
    WEEKDAY_MASS("Werktagsmesse"),
    LITURGY_OF_THE_WORD("Wortgottesdienst"),
    BAPTISM("Taufe"),
    SPECIAL_SERVICE("Sonderdienst"),
    WORSHIP("Gottesdienst (undefiniert)");

    private final String label;

}
