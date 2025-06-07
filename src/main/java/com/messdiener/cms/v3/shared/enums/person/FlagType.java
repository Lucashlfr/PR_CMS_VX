package com.messdiener.cms.v3.shared.enums.person;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlagType {

    ONBOARDING_DATA(true, false, "Rezertifizierung"),
    ONBOARDING_PRIVACY(true,false, "Datenschutzerklärung"),
    ONBOARDING_EMERGENCY_CONTACT(true,false, "Notfallkontakte"),
    ONBOARDING_SAE(true,false, "SAE"),
    PREVENTION_TRAINING(true,true, "Präventionsschulung"),
    CUSTOM(false,true,"");

    private final boolean defaultCreated;
    private final boolean editable;
    private final String label;
}
