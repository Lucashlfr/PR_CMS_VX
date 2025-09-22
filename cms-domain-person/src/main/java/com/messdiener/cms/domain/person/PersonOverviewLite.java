package com.messdiener.cms.domain.person;

import com.messdiener.cms.shared.enums.tenant.Tenant;
import java.util.UUID;

/** Schlankes DTO nur für Abfragen über Modulgrenzen. */
public record PersonOverviewLite(
        UUID id,
        String firstName,
        String lastName,
        Tenant tenant
) {}
