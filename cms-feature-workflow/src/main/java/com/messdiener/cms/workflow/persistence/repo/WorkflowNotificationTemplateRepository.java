package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.NotificationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowNotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, UUID> {
    List<NotificationTemplateEntity> findByKeyOrderByVersionDesc(String key);
}
