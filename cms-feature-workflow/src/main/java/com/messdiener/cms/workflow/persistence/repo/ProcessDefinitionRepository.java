package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.ProcessDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinitionEntity, UUID> {}
