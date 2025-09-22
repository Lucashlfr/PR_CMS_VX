// X:\workspace\PR_CMS\cms-feature-auth\src\main\java\com\messdiener\cms\auth\app\service\PersistentLoginService.java
package com.messdiener.cms.auth.persistence.service;

import com.messdiener.cms.auth.persistence.map.PersistentLoginMapper;
import com.messdiener.cms.auth.persistence.repo.PersistentLoginRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class PersistentLoginService implements PersistentTokenRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentLoginService.class);

    private final PersistentLoginRepository repository;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        try {
            repository.save(PersistentLoginMapper.toEntity(token));
        } catch (Exception e) {
            LOGGER.error("createNewToken failed for user={}", token.getUsername(), e);
        }
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        try {
            repository.findBySeries(series).ifPresent(entity -> {
                entity.setToken(tokenValue);
                entity.setLastUsed(lastUsed != null ? lastUsed.toInstant() : Instant.now());
                repository.save(entity);
            });
        } catch (Exception e) {
            LOGGER.error("updateToken failed for series={}", series, e);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            return repository.findBySeries(seriesId)
                    .map(PersistentLoginMapper::toToken)
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("getTokenForSeries failed for series={}", seriesId, e);
            return null;
        }
    }

    @Override
    public void removeUserTokens(String username) {
        try {
            repository.deleteByUsername(username);
        } catch (Exception e) {
            LOGGER.error("removeUserTokens failed for user={}", username, e);
        }
    }
}
