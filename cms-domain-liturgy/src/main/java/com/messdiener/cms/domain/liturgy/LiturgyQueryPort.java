// LiturgyQueryPort: was du im Controller brauchst
package com.messdiener.cms.domain.liturgy;

import com.messdiener.cms.domain.person.PersonOverviewLite;
import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.shared.enums.tenant.Tenant;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface LiturgyQueryPort {
    List<LiturgyView> getLiturgies(Tenant tenant, long start, long end);

    Map<UUID, Map<UUID, LiturgieState>> getStatesForLiturgies(List<LiturgyView> liturgies, List<PersonOverviewLite> persons);

    /** Einzelelement per ID laden. */
    Optional<LiturgyView> getById(UUID id);

    /** Alle Personen mit ihrem State für eine Liturgie im angegebenen Tenant. */
    List<PersonStateView> getPersonStatesForLiturgy(Tenant tenant, UUID liturgyId);
}
