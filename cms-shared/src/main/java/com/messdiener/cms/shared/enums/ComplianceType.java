package com.messdiener.cms.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ComplianceType {

    SAE("Fehlende Selbstauskunftserklärung", 10, "Die betreffende Person ist als Betreuer (im Sinne des Präventionskonzept) hinterlegt. Aktuell fehlt die SAE.", "Workflow SAE ausfüllen"),
    PREVENTION_TRAINING("Fehlende Präventionsschulung", 10, "Die betreffende Person ist als Betreuer (im Sinne des Präventionskonzept) hinterlegt. Aktuell fehlt die Präventionsschulung.", "Präventionsschulung nachholen"),
    PRIVACY("Fehlende Datenschutzerklärung", 8, "Die betreffende Person hat die Datenschutzerklärung noch nicht ausgefüllt.", "Workflow Onboarding ausfüllen"),
    CONTACT("Fehlende Notfallkontakte", 6, "Die betreffende Person hat keine Notfallkontakte angegeben.", "Workflow Onboarding ausfüllen"),
    DATA("Fehlende Rezertifizierung", 2, "Die betreffende Person hat ihre Daten nicht aktualisiert.", "Workflow Onboarding ausfüllen");

    private final String title;
    private final int score;
    private final String description;
    private final String action;

}
