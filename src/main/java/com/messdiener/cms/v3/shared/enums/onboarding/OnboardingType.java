package com.messdiener.cms.v3.shared.enums.onboarding;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnboardingType {

    PERSONAL_INFORMATION("Persönliche Informationen"),
    EMERGENCY_CONTACT("Notfallkontakt"),
    PRIVACY_POLICY("Datenschutz"),
    SAE("Selbstauskunftserklärung"),
    NULL("Null");

    private final String label;

}
