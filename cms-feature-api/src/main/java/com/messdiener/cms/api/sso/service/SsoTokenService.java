package com.messdiener.cms.api.sso.service;

import com.messdiener.cms.api.sso.dto.SsoProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class SsoTokenService {

    private final SsoProperties props;
    private final Key key;

    public SsoTokenService(SsoProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.ttlSeconds());
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String buildSsoUrl(String token, String targetPathWithQuery) {
        String target = targetPathWithQuery.startsWith("/") ? targetPathWithQuery : "/" + targetPathWithQuery;
        String t  = URLEncoder.encode(token,  StandardCharsets.UTF_8);
        String tg = URLEncoder.encode(target, StandardCharsets.UTF_8);
        return props.webBaseUrl() + "/sso?token=" + t + "&target=" + tg;
    }

    public String validateAndGetUser(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
