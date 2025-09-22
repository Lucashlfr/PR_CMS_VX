// cms-domain-person/src/main/java/com/messdiener/cms/domain/person/PersonSessionView.java
package com.messdiener.cms.domain.person;

import java.util.UUID;

public record PersonSessionView(
        UUID id,
        String firstName,      // NEU
        String lastName,       // NEU
        String tenantName,
        int fRank,
        String imgAddress
) {}
