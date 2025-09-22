// X:\workspace\PR_CMS\cms-feature-auth\src\main\java\com\messdiener\cms\auth\persistence\map\PersistentLoginMapper.java
package com.messdiener.cms.auth.persistence.map;

import com.messdiener.cms.auth.persistence.entity.PersistentLoginEntity;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.time.Instant;
import java.util.Date;

public final class PersistentLoginMapper {

    private PersistentLoginMapper() {}

    public static PersistentLoginEntity toEntity(PersistentRememberMeToken token) {
        return PersistentLoginEntity.builder()
                .series(token.getSeries())
                .username(token.getUsername())
                .token(token.getTokenValue())
                .lastUsed(token.getDate() != null ? token.getDate().toInstant() : Instant.now())
                .build();
    }

    public static PersistentRememberMeToken toToken(PersistentLoginEntity e) {
        return new PersistentRememberMeToken(
                e.getUsername(),
                e.getSeries(),
                e.getToken(),
                Date.from(e.getLastUsed() != null ? e.getLastUsed() : Instant.now())
        );
    }
}
