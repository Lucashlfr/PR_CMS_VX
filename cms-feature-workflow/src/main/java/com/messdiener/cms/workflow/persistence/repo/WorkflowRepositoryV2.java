package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.workflow.persistence.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface WorkflowRepositoryV2 extends JpaRepository<WorkflowEntity, UUID> {

    List<WorkflowEntity> findAllByOrderByCreationDateDesc();

    List<WorkflowEntity> findByStateOrderByCreationDateDesc(CMSState state);

    List<WorkflowEntity> findByAssigneeIdOrderByCreationDateDesc(UUID assigneeId);

    List<WorkflowEntity> findByApplicantIdOrderByCreationDateDesc(UUID applicantId);

    List<WorkflowEntity> findByProcessKeyAndProcessVersionOrderByCreationDateDesc(String processKey, int processVersion);

    List<WorkflowEntity> findByTitleContainingIgnoreCaseOrderByCreationDateDesc(String queryPart);
}
