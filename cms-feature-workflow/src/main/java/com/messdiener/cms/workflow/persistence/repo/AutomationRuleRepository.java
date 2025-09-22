package com.messdiener.cms.workflow.persistence.repo;

import com.messdiener.cms.workflow.persistence.entity.AutomationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AutomationRuleRepository extends JpaRepository<AutomationRuleEntity, UUID> {}
