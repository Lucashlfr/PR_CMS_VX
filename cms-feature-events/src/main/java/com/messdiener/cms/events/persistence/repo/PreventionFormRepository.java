// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\repo\PreventionFormRepository.java
package com.messdiener.cms.events.persistence.repo;

import com.messdiener.cms.events.persistence.entity.PreventionFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PreventionFormRepository extends JpaRepository<PreventionFormEntity, UUID> { }
