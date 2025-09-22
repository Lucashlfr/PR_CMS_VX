package com.messdiener.cms.notifications.api.service;


import com.messdiener.cms.notifications.api.dto.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /** Sende eine Notification an einen User (persistiert + WebSocket + Push) und liefere das DTO zurück. */
    NotificationDto sendToUser(UUID userId, String title, String body, String dataJson);

    /** Liste die letzten 50 Notifications eines Users (absteigend nach createdAt). */
    List<NotificationDto> listLatest(UUID userId);

    /** Markiere eine Notification des Users als gelesen (setzt readAt, prüft Ownership). */
    void markRead(UUID userId, UUID notificationId);

    /** Registriere/aktualisiere ein Device-Token für den User (Upsert, revoke=false). */
    void registerDeviceToken(UUID userId, String token, String platform);
}
