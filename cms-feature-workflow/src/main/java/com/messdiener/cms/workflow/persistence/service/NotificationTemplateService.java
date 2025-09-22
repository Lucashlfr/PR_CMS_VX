package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.workflow.domain.dto.NotificationTemplateDTO;
import com.messdiener.cms.workflow.persistence.map.WorkflowNotificationTemplateMapper;
import com.messdiener.cms.workflow.persistence.repo.WorkflowNotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final WorkflowNotificationTemplateRepository repo;

    public NotificationTemplateDTO save(NotificationTemplateDTO dto) {
        var saved = repo.save(WorkflowNotificationTemplateMapper.toEntity(dto));
        return WorkflowNotificationTemplateMapper.toDto(saved);
    }

    public Optional<NotificationTemplateDTO> getById(UUID id) {
        return repo.findById(id).map(WorkflowNotificationTemplateMapper::toDto);
    }

    public List<NotificationTemplateDTO> getByKey(String key) {
        return repo.findByKeyOrderByVersionDesc(key).stream()
                .map(WorkflowNotificationTemplateMapper::toDto)
                .collect(Collectors.toList());
    }
}
