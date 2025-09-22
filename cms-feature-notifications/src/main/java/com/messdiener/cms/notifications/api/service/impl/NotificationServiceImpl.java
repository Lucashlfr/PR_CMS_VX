package com.messdiener.cms.notifications.api.service.impl;

import com.messdiener.cms.notifications.api.dto.NotificationDto;
import com.messdiener.cms.notifications.api.entity.DeviceToken;
import com.messdiener.cms.notifications.api.notifications.entity.Notification;
import com.messdiener.cms.notifications.api.repo.DeviceTokenRepository;
import com.messdiener.cms.notifications.api.repo.NotificationRepository;
import com.messdiener.cms.notifications.api.service.NotificationService;
import com.messdiener.cms.notifications.api.service.PushGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final DeviceTokenRepository tokenRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final PushGateway pushGateway;

    @Override
    @Transactional
    public NotificationDto sendToUser(UUID userId, String title, String body, String dataJson) {
        // 1) Persist
        Notification entity = Notification.builder()
                .userId(userId)
                .title(Objects.requireNonNullElse(title, ""))
                .body(Objects.requireNonNullElse(body, ""))
                .dataJson(dataJson)
                .createdAt(Instant.now())
                .build();
        entity = notificationRepo.save(entity);

        NotificationDto dto = toDto(entity);

        // 2) WebSocket (foreground)
        messagingTemplate.convertAndSend("/topic/user." + userId, dto);

        // 3) Push (background)
        List<String> tokens = tokenRepo.findByUserIdAndRevokedFalse(userId)
                .stream()
                .map(DeviceToken::getToken)
                .toList();
        if (!tokens.isEmpty()) {
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", entity.getId().toString());
            if (dataJson != null) data.put("dataJson", dataJson);

            pushGateway.send(tokens, entity.getTitle(), entity.getBody(), data);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> listLatest(UUID userId) {
        return notificationRepo.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification entity = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("notification not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new SecurityException("forbidden");
        }
        if (entity.getReadAt() == null) {
            entity.setReadAt(Instant.now());
            notificationRepo.save(entity);
        }
    }

    /**
     * Idempotente & konfliktarme Registrierung eines Device-Tokens:
     * - Ein aktiver Datensatz pro (userId, platform)
     * - Kein Write, wenn sich der Token nicht geändert hat (vermeidet unnötige Versionserhöhungen)
     * - OptimisticLocking wird abgefangen und geloggt (kein 500)
     */
    @Override
    @Transactional
    public void registerDeviceToken(UUID userId, String token, String platformRaw) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is blank");
        }

        final String platform = platformRaw == null
                ? "ANDROID"
                : platformRaw.toUpperCase(Locale.ROOT);

        try {
            // 1) Primär nach (userId, platform, !revoked) suchen
            Optional<DeviceToken> byUserAndPlatform = tokenRepo.findByUserIdAndPlatformAndRevokedFalse(userId, platform);

            if (byUserAndPlatform.isPresent()) {
                DeviceToken existing = byUserAndPlatform.get();

                // Idempotenz: Token unverändert -> gar nicht speichern (vermeidet Version-Update & Lock-Konflikte)
                if (token.equals(existing.getToken())) {
                    return;
                }

                existing.setToken(token);
                existing.setLastSeenAt(Instant.now());
                existing.setRevoked(false);
                tokenRepo.save(existing);
                return;
            }

            // 2) Fallback: Der Token existiert bereits (anderem User zugeordnet?) -> übernehmen
            Optional<DeviceToken> byToken = tokenRepo.findByToken(token);
            if (byToken.isPresent()) {
                DeviceToken dt = byToken.get();
                dt.setUserId(userId);
                dt.setPlatform(platform);
                dt.setLastSeenAt(Instant.now());
                dt.setRevoked(false);
                tokenRepo.save(dt);
                return;
            }

            // 3) Neu anlegen
            DeviceToken created = DeviceToken.builder()
                    .userId(userId)
                    .token(token)
                    .platform(platform)
                    .createdAt(Instant.now())
                    .lastSeenAt(Instant.now())
                    .revoked(false)
                    .build();
            tokenRepo.save(created);

        } catch (OptimisticLockingFailureException e) {
            // Kein Crash mehr – beim nächsten Lebenszeichen wird erneut registriert.
            log.warn("registerDeviceToken optimistic-lock conflict (user={}, platform={}) – ignored",
                    userId, platform, e);
        } catch (org.hibernate.StaleObjectStateException e) {
            // Falls direkt aus Hibernate bubbelt
            log.warn("registerDeviceToken stale object (user={}, platform={}) – ignored",
                    userId, platform, e);
        }
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getDataJson(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}
