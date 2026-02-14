// cms-feature-api/src/main/java/com/messdiener/cms/api/sso/controller/SsoLinkController.java
package com.messdiener.cms.api.sso.controller;

import com.messdiener.cms.api.sso.service.SsoTokenService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sso")
public class SsoLinkController {

    private final SsoTokenService tokens;

    public SsoLinkController(SsoTokenService tokens) {
        this.tokens = tokens;
    }

    @PostMapping(
            value = "/link",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> link(@RequestBody Map<String, String> body, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }
        String username = auth.getName();
        String target = body.getOrDefault("target", "/");
        String token = tokens.createToken(username);
        String url   = tokens.buildSsoUrl(token, target);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
