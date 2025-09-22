package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.workflow.domain.dto.DocumentationDTO;
import com.messdiener.cms.workflow.persistence.map.DocumentationMapper;
import com.messdiener.cms.workflow.persistence.repo.DocumentationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentationService {

    private final DocumentationRepository repo;

    public DocumentationDTO save(DocumentationDTO dto) {
        var saved = repo.save(DocumentationMapper.toEntity(dto));
        return DocumentationMapper.toDto(saved);
    }

    public Optional<DocumentationDTO> getById(UUID id) {
        return repo.findById(id).map(DocumentationMapper::toDto);
    }

    public List<DocumentationDTO> getByWorkflow(UUID workflowId) {
        return repo.findByWorkflowIdOrderByCreatedAtDesc(workflowId).stream()
                .map(DocumentationMapper::toDto).toList();
    }
}
