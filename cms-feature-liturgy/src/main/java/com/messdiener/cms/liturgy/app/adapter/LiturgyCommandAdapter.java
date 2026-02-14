package com.messdiener.cms.liturgy.app.adapter;

import com.messdiener.cms.domain.liturgy.LiturgyCommandPort;
import com.messdiener.cms.liturgy.persistence.service.LiturgieMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiturgyCommandAdapter implements LiturgyCommandPort {

    private final LiturgieMappingService liturgieMappingService;

    @Override
    public void unassignWithReason(UUID personId, UUID liturgyId, String reason) {
        // Aktuell: setzt State auf CANCELED. Reason könnte durch eigene Auditierung persistiert werden.
        liturgieMappingService.unassignWithReason(personId, liturgyId, reason);
    }
}
