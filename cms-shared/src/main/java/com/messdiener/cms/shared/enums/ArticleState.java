package com.messdiener.cms.shared.enums;

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
