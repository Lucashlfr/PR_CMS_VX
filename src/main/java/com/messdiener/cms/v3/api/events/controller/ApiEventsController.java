package com.messdiener.cms.v3.api.events.controller;

import com.messdiener.cms.v3.api.common.response.ApiResponseHelper;
import com.messdiener.cms.v3.api.common.response.dto.ApiResponse;
import com.messdiener.cms.v3.api.events.dto.AppointmentDto;
import com.messdiener.cms.v3.api.events.service.AppointmentsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiEventsController {

    private final AppointmentsService appointmentsService;

    /**
     * GET /api/events/next?limit=10
     * Liefert die nächsten Termine als JSON-Envelope (data/meta).
     */
    @GetMapping("/next")
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> next(
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            HttpServletRequest request,
            Locale locale
    ) {
        if (limit <= 0) limit = 10;
        List<AppointmentDto> items = appointmentsService.findUpcoming(limit);
        return ApiResponseHelper.ok(items, request, locale);
    }
}
