// src/main/java/com/messdiener/cms/v3/api/notifications/repo/NotificationRepository.java
package com.messdiener.cms.v3.api.notifications.repo;

import com.messdiener.cms.v3.api.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}
