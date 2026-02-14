package com.messdiener.cms.api.events.controller;

import com.messdiener.cms.api.common.response.ApiResponse;
import com.messdiener.cms.api.common.response.ApiResponseHelper;
import com.messdiener.cms.domain.events.AppointmentItem;
import com.messdiener.cms.domain.events.AppointmentsQueryPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

/**
 * REST-API für Termine (Events).
 * Gibt Domain-DTOs im ApiResponse-Envelope ("data") zurück.
 */
@RestController
@RequestMapping(value = "/api/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiEventsController {

    private final AppointmentsQueryPort appointments; // Domain-Port!

    /**
     * GET /api/events/next?limit=10
     * Liefert die nächsten Termine als JSON-Envelope (data/meta).
     */
    @GetMapping("/next")
    public ResponseEntity<ApiResponse<List<AppointmentItem>>> next(
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            HttpServletRequest request,
            Locale locale
    ) {
        if (limit <= 0) limit = 3;
        List<AppointmentItem> items = appointments.findUpcoming(limit);
        return ApiResponseHelper.ok(items, request, locale);
    }

    /**
     * GET /api/events
     * Liefert alle Termine (für die Übersichtsliste).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentItem>>> all(
            HttpServletRequest request,
            Locale locale
    ) {
        List<AppointmentItem> items = appointments.findAll();
        return ApiResponseHelper.ok(items, request, locale);
    }
}
