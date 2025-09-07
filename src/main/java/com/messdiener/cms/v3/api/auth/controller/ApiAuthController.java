// src/main/java/com/messdiener/cms/v3/api/auth/controller/ApiAuthController.java
package com.messdiener.cms.v3.api.auth.controller;

import com.messdiener.cms.v3.api.auth.dto.LoginRequest;
import com.messdiener.cms.v3.api.auth.dto.LoginResponse;
import com.messdiener.cms.v3.api.common.response.ApiResponseHelper;
import com.messdiener.cms.v3.api.common.response.dto.ApiResponse;
import com.messdiener.cms.v3.security.SecurityHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;

    /**
     * Optional – nur wenn RememberMe konfiguriert ist. Darf null sein.
     */
    private final RememberMeServices rememberMeServices;

    /**
     * Zugriff auf die aktuelle Person (Tenant, Vorname/Nachname, etc.)
     */
    private final SecurityHelper securityHelper;

    /**
     * POST /api/auth/login
     * Erwartet JSON { "username": "...", "password": "..." }
     * Setzt SecurityContext + triggert RememberMe (cmslogin), wenn konfiguriert.
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Validated @RequestBody LoginRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response,
            Locale locale
    ) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(requestBody.username(), requestBody.password());
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // RememberMe-Cookie immer setzen (dauerhaft eingeloggt)
        if (rememberMeServices != null) {
            rememberMeServices.loginSuccess(request, response, auth);
        }

        var result = new LoginResponse(true, auth.getName());
        return ApiResponseHelper.ok(result, request, locale);
    }

    /**
     * GET /api/auth/me
     * Gibt kompakten Status zurück und enthält jetzt auch "firstname"/"lastname"/"displayName".
     *
     * Beispiel:
     * {
     *   "data": {
     *     "ok": true,
     *     "username": "xcg5415",
     *     "firstname": "Max",
     *     "lastname": "Mustermann",
     *     "displayName": "Max Mustermann"
     *   },
     *   "meta": { ... }
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(
            HttpServletRequest request,
            Locale locale
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean ok = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        String username = ok ? auth.getName() : "";

        Map<String, Object> data = new HashMap<>();
        data.put("ok", ok);
        data.put("username", username);

        // Versuche, die aktuell eingeloggte Person zu bestimmen
        securityHelper.getPerson().ifPresent(person -> {
            String first = safe(person.getFirstname());
            String last = safe(person.getLastname());
            String display = (first + " " + last).trim();

            data.put("firstname", first);
            data.put("lastname", last);
            data.put("displayName", display);
            // Optional nützlich:
            data.put("id", person.getId());
            if (person.getTenant() != null) {
                data.put("tenantId", person.getTenant().toString());
                data.put("tenantName", person.getTenant().getName());
            }
        });

        return ApiResponseHelper.ok(data, request, locale);
    }

    /**
     * POST /api/auth/logout
     * Invalidiert Session + löscht RememberMe-Cookie (cmslogin) und ggf. dein Session-Cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Locale locale
    ) {
        // Session invalidieren
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // RememberMe löschen (Cookie-Name muss mit deiner SecurityConfig übereinstimmen)
        deleteCookie(response, "cmslogin");

        // Falls du zusätzlich das Session-Cookie löschen willst:
        deleteCookie(response, "CentralManagementSystem_vX");

        Map<String, Object> data = Map.of("ok", true);
        return ApiResponseHelper.ok(data, request, locale);
    }

    private static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
