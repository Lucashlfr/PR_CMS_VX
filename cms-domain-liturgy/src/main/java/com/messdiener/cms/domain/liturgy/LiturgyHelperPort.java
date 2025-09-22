package com.messdiener.cms.domain.liturgy;

import com.messdiener.cms.shared.enums.tenant.Tenant;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiturgyHelperPort {

    /**
     * Baut die Model-Attribute für Übersichten auf (ersetzt extractedLoadMethod).
     */
    void loadOverview(Model model,
                      Optional<String> startDateS,
                      Optional<String> endDateS,
                      Tenant tenantOfViewer,
                      Optional<UUID> targetPersonId);

    List<EventParticipationDto> getParticipation(UUID tenant,
                                                 Optional<String> start,
                                                 Optional<String> end,
                                                 Optional<UUID> onlyPerson);
}
