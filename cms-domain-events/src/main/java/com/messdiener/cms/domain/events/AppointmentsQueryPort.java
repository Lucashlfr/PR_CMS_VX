package com.messdiener.cms.domain.events;

import java.util.List;

/**
 * Domain-Port: liefert Termine für API/Use-Cases.
 * Implementierung erfolgt im Feature-Modul (cms-feature-events).
 */
public interface AppointmentsQueryPort {

    /** Kommende Termine (limit >= 1) */
    List<AppointmentItem> findUpcoming(int limit);

    /** Alle Termine (z. B. für Übersicht) */
    List<AppointmentItem> findAll();
}
