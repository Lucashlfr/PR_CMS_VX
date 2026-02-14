// cms-domain-person/src/main/java/com/messdiener/cms/domain/person/PersonSessionView.java
package com.messdiener.cms.domain.person;

import com.messdiener.cms.shared.enums.tenant.Tenant;

import java.util.UUID;

public record PersonSessionView(
        UUID id,
        String firstName,      // NEU
        String lastName,       // NEU
        String tenantName,
        Tenant tenant,
        int fRank,
        String imgAddress
) {}
