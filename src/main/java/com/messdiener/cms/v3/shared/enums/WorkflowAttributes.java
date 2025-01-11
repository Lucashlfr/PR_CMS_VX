package com.messdiener.cms.v3.shared.enums;

import lombok.Getter;

public class WorkflowAttributes {

    @Getter
    public enum WorkflowType {

        SCHEDULER("Dienstplan"),
        DATA("Daten überprüfen"),
        PRIVACY_POLICY("Datenschutzformular"),
        NULL("Fehler");

        final String label;

        WorkflowType(String label) {
            this.label = label;
        }
    }

    @Getter
    public enum WorkflowState {

        PENDING("Ausstehend"),
        COMPLETED("Erledigt");

        final String label;

        WorkflowState(String label) {
            this.label = label;
        }
    }

}
