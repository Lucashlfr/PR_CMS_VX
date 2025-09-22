package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.domain.dto.NotificationDTO;
import com.messdiener.cms.workflow.persistence.map.WorkflowNotificationMapper;
import com.messdiener.cms.workflow.persistence.repo.WorkflowNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowNotificationService {

    private final WorkflowNotificationRepository repo;

    public NotificationDTO save(NotificationDTO dto) {
        var saved = repo.save(WorkflowNotificationMapper.toEntity(dto));
        return WorkflowNotificationMapper.toDto(saved);
    }

    public Optional<NotificationDTO> getById(UUID id) {
        return repo.findById(id).map(WorkflowNotificationMapper::toDto);
    }

    public List<NotificationDTO> getByState(CMSState state) {
        return repo.findByStateOrderByNotificationIdDesc(state).stream()
                .map(WorkflowNotificationMapper::toDto).toList();
    }

    public List<NotificationDTO> getByTemplate(UUID templateId) {
        return repo.findByTemplateIdOrderByNotificationIdDesc(templateId).stream()
                .map(WorkflowNotificationMapper::toDto).toList();
    }
}
