package com.messdiener.cms.v3.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArticleType {

    BLOG("Pressemeldung", false),
    INTERN("Anmeldung", false),
    ABOUT("Über uns", true),
    CONTACT("Kontakt", true),
    IMPRESSUM("Impressum", true),
    NULL("Null", false),;

    private final String label;
    private boolean autoCreate;

}
