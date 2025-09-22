package com.messdiener.cms.notifications.api.repo;

// src/main/java/com/messdiener/cms/v3/api/notifications/repo/DeviceTokenRepository.java

import com.messdiener.cms.notifications.api.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findByUserIdAndRevokedFalse(UUID userId);

    Optional<DeviceToken> findByToken(String token);

    // Neu: genau ein aktiver Eintrag pro (userId, platform)
    Optional<DeviceToken> findByUserIdAndPlatformAndRevokedFalse(UUID userId, String platform);
}
