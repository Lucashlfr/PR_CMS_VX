package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.FormSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormSubmissionRepository extends JpaRepository<FormSubmissionEntity, UUID> {
    List<FormSubmissionEntity> findByWorkflowIdOrderBySubmittedAtDesc(UUID workflowId);
    List<FormSubmissionEntity> findByFormKeyAndFormVersionOrderBySubmittedAtDesc(String formKey, Integer formVersion);
}
