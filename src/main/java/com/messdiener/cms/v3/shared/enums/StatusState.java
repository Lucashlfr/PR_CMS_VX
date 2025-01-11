package com.messdiener.cms.v3.shared.enums;

import lombok.Getter;

@Getter
public enum StatusState {
    EDIT_OK("Änderungen wurden gespeichert"),
    CREATE_OK("Person wurde erfolgreich angelegt"),
    SCHEDULER_OK("Workflow Dienstplan Abfrage wurde erfolgreich beendet."),
    CHECK_OK("Workflow Datenabgleich Abfrage wurde erfolgreich beendet."),
    PRIVACY_POLICY_OK("Workflow Datenschutz Abfrage wurde erfolgreich beendet."),;

    final String label;

    StatusState(String label) {
        this.label = label;
    }
}
