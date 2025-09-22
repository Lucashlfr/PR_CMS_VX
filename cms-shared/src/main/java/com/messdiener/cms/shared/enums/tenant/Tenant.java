package com.messdiener.cms.shared.enums.tenant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Tenant {

    MSNB("Messdiener St. Nikolaus Bellheim"),
    MSGK("Messdiener St. Georg Knittelsheim"),
    MSMO("Messdiener St. Martin Ottersheim"),
    MSTM("Messdiener technischer Mandant"),
    PHBB("Pfarrei Hl. Hildegard von Bingen"),
    MSNN("Null");

    private final String name;

}
