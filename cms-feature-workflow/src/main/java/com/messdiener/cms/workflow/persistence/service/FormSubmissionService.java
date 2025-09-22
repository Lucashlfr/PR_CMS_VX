package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.workflow.domain.dto.FormSubmissionDTO;
import com.messdiener.cms.workflow.persistence.map.FormSubmissionMapper;
import com.messdiener.cms.workflow.persistence.repo.FormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormSubmissionService {

    private final FormSubmissionRepository repo;

    public FormSubmissionDTO save(FormSubmissionDTO dto) {
        var saved = repo.save(FormSubmissionMapper.toEntity(dto));
        return FormSubmissionMapper.toDto(saved);
    }

    public Optional<FormSubmissionDTO> getById(UUID id) {
        return repo.findById(id).map(FormSubmissionMapper::toDto);
    }

    public List<FormSubmissionDTO> getByWorkflow(UUID workflowId) {
        return repo.findByWorkflowIdOrderBySubmittedAtDesc(workflowId).stream()
                .map(FormSubmissionMapper::toDto).toList();
    }

    public List<FormSubmissionDTO> getByFormAndVersion(String key, int version) {
        return repo.findByFormKeyAndFormVersionOrderBySubmittedAtDesc(key, version).stream()
                .map(FormSubmissionMapper::toDto).toList();
    }
}
