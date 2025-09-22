package com.messdiener.cms.auth.api.controller;

import com.messdiener.cms.api.common.response.ApiResponse;
import com.messdiener.cms.api.common.response.ApiResponseHelper;
import com.messdiener.cms.auth.api.dto.LoginRequest;
import com.messdiener.cms.auth.api.dto.LoginResponse;
import com.messdiener.cms.web.common.security.SecurityHelper; // <-- neu
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
    private final RememberMeServices rememberMeServices; // darf null sein
    private final SecurityHelper securityHelper;         // aus web-common

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Validated @RequestBody LoginRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response,
            Locale locale
    ) {
        var token = new UsernamePasswordAuthenticationToken(requestBody.username(), requestBody.password());
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        if (rememberMeServices != null) {
            rememberMeServices.loginSuccess(request, response, auth);
        }

        var result = new LoginResponse(true, auth.getName());
        return ApiResponseHelper.ok(result, request, locale);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(HttpServletRequest request, Locale locale) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean ok = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        String username = ok ? auth.getName() : "";

        Map<String, Object> data = new HashMap<>();
        data.put("ok", ok);
        data.put("username", username);

        securityHelper.getPerson().ifPresent(p -> {
            String first = safe(p.firstName());
            String last  = safe(p.lastName());
            String display = (first + " " + last).trim();

            data.put("firstname", first);
            data.put("lastname", last);
            data.put("displayName", display);
            data.put("id", p.id());

            if (p.tenantName() != null) {
                data.put("tenantName", p.tenantName());
            }
        });

        return ApiResponseHelper.ok(data, request, locale);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Locale locale
    ) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        deleteCookie(response, "cmslogin");
        deleteCookie(response, "CentralManagementSystem_vX");

        return ApiResponseHelper.ok(Map.of("ok", true), request, locale);
    }

    private static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
