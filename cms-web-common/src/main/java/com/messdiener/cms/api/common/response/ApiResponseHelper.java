package com.messdiener.cms.api.common.response;

import com.messdiener.cms.api.common.response.dto.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Einheitlicher API-Envelope:
 * {
 *   "data": ...,
 *   "meta": { "issuedAt", "requestId", "path", "locale" }
 * }
 *
 * Als Spring-Bean registriert, damit @RequiredArgsConstructor-Injection in Controllern funktioniert.
 * Bestehende statische Methoden bleiben erhalten.
 */
@Component
public class ApiResponseHelper {

    // Wichtig: Kein privater Konstruktor mehr, damit Spring das Bean erstellen kann.
    public ApiResponseHelper() {
    }

    /** Direkt als ResponseEntity.ok(...) zurückgeben. */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, HttpServletRequest request, Locale locale) {
        return ResponseEntity.ok(build(data, request, locale));
    }

    /** Baut einen ApiResponse<T> inkl. Meta aus Request/Locale. */
    public static <T> ApiResponse<T> build(T data, HttpServletRequest request, Locale locale) {
        final String requestId = UUID.randomUUID().toString();
        final String path = (request != null && request.getRequestURI() != null) ? request.getRequestURI() : "";
        final String loc = toLocaleTag(locale);
        final Meta meta = new Meta(Instant.now().toEpochMilli(), requestId, path, loc);
        return new ApiResponse<>(data, meta);
    }

    /** Überladung ohne HttpServletRequest. */
    public static <T> ApiResponse<T> build(T data, String path, String localeTag, String requestId) {
        final String rid = (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;
        final String loc = (localeTag == null || localeTag.isBlank()) ? "de_DE" : localeTag;
        final Meta meta = new Meta(Instant.now().toEpochMilli(), rid, path == null ? "" : path, loc);
        return new ApiResponse<>(data, meta);
    }

    private static String toLocaleTag(Locale locale) {
        if (locale == null) return "de_DE";
        return locale.toLanguageTag().replace('-', '_');
    }
}
