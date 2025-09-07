package com.messdiener.cms.v3.api.events.dto;

/**
 * Schlankes DTO, das gut zu deinem Flutter-Model passt.
 * Alle Strings, damit die App ohne zusätzliche Formatierung anzeigen kann.
 */
public record AppointmentDto(
        String id,
        String title,
        String date,       // z.B. "2025-08-21"
        String time,       // z.B. "18:30"
        String location,
        String description
) {}
