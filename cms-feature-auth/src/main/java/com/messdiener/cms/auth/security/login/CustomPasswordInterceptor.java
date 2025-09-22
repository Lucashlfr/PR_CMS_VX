// cms-feature-auth/src/main/java/com/messdiener/cms/auth/security/login/CustomPasswordInterceptor.java
package com.messdiener.cms.auth.security.login;

import com.messdiener.cms.domain.auth.AuthContextPort;
import com.messdiener.cms.domain.person.PersonPasswordQueryPort;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class CustomPasswordInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CustomPasswordInterceptor.class);

    private final AuthContextPort authContextPort;
    private final PersonPasswordQueryPort personPasswordQueryPort;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Nicht eingreifen bei ERROR/ASYNC/FORWARD
        DispatcherType dt = request.getDispatcherType();
        if (dt == DispatcherType.ERROR || dt == DispatcherType.FORWARD || dt == DispatcherType.ASYNC) {
            return true;
        }

        final String uri = request.getRequestURI();

        // Eigene Ziel-/Login-Seiten nicht erneut abfangen
        if (uri.equals("/security/customPw") || uri.equals("/login") || uri.equals("/login-error")) {
            return true;
        }

        // Nur bei eingeloggten Nutzern prüfen
        String username = authContextPort.getUsername();
        if (username == null || "anonymousUser".equals(username)) {
            return true;
        }

        // Custom-Passwort vorhanden?
        boolean hasCustom = personPasswordQueryPort.hasCustomPassword(username);
        if (hasCustom) {
            return true;
        }

        // -> Kein Custom-PW: je nach Request-Typ unterschiedlich reagieren
        if (isHtmlNavigation(request)) {
            log.debug("User '{}' has no custom password -> redirect to /security/customPw (HTML)", username);
            response.sendRedirect("/security/customPw");
        } else {
            log.debug("User '{}' has no custom password -> 403 Forbidden (API/AJAX)", username);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }

    /**
     * Ermittelt, ob es sich um eine „normale“ HTML-Navigation handelt.
     * Kriterien:
     * - Accept-Header enthält text/html (klassische Seitenaufrufe)
     * - KEIN XMLHttpRequest (X-Requested-With)
     * - KEINE expliziten JSON-/Fetch-Indikatoren
     */
    private boolean isHtmlNavigation(HttpServletRequest request) {
        String accept = headerLower(request, "Accept");
        String xrw = headerLower(request, "X-Requested-With");
        String contentType = headerLower(request, "Content-Type");

        // Klassische Browser-Navigation erkennt man am Accept: text/html
        boolean acceptsHtml = accept.contains("text/html");

        // AJAX/fetch Indikatoren
        boolean isXmlHttp = xrw.contains("xmlhttprequest");
        boolean isJsonBody = contentType.contains("application/json");

        // Viele fetch()-Requests senden Accept: */* -> dann NICHT als HTML behandeln
        boolean acceptLooksLikeFetch = accept.equals("*/*");

        // HTML-Navigation: erwartet HTML, ist kein AJAX, kein JSON-Body, kein fetch-*/*
        return acceptsHtml && !isXmlHttp && !isJsonBody && !acceptLooksLikeFetch;
    }

    private String headerLower(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        return v == null ? "" : v.toLowerCase();
    }
}
