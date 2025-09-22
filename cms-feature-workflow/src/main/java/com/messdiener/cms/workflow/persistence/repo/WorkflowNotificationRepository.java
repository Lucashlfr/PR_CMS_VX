package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByStateOrderByNotificationIdDesc(CMSState state);
    List<NotificationEntity> findByTemplateIdOrderByNotificationIdDesc(UUID templateId);
}
