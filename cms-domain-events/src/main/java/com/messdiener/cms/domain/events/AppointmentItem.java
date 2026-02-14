package com.messdiener.cms.domain.events;

/**
 * Domain-DTO für Termine (Events), exakt passend zum gewünschten API-Output.
 * Alle Strings, damit Frontends kein zusätzliches Mapping benötigen.
 */
public record AppointmentItem(
        String title,
        String date,
        String location,
        String status,   // "open" | "confirmed" | "canceled"
        String imageUrl
) {}
