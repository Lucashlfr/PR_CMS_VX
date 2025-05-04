package com.messdiener.cms.v3.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArticleState {

    CREATED("Erstellt"),
    PUBLISHED("Veröffentlicht"),
    ARCHIVED("Archiviert"),
    NULL("Null");

    private final String label;

}
