package com.messdiener.cms.v3.shared.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionType {

    ACCOUNT("Konto"), CASH("Barkasse");

    private final String label;

}
