package com.messdiener.cms.api.sso.controller;

import com.messdiener.cms.api.sso.service.SsoTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class SsoWebController {

    private final SsoTokenService tokens;

    public SsoWebController(SsoTokenService tokens) {
        this.tokens = tokens;
    }

    @GetMapping("/sso")
    public void sso(@RequestParam("token") String token,
                    @RequestParam("target") String target,
                    HttpServletRequest request,
                    HttpServletResponse response) throws IOException {

        // 1) Token prüfen → username
        String username = tokens.validateAndGetUser(token);

        // 2) SecurityContext + Session setzen
        var auth = new UsernamePasswordAuthenticationToken(
                username, "N/A", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        var context = new SecurityContextImpl(auth);
        request.getSession(true);
        request.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        // 3) Weiterleitung ins Ziel (z. B. /workflow/form?id=…)
        response.sendRedirect(target);
    }
}
