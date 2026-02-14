package com.messdiener.cms.domain.liturgy;

import java.util.UUID;

public interface LiturgyCommandPort {
    /**
     * Entfernt die Zuordnung der Person zur Liturgie mit Begründung.
     * Implementierung entscheidet, wie/wo der Grund persistiert oder auditiert wird.
     */
    void unassignWithReason(UUID personId, UUID liturgyId, String reason);
}
