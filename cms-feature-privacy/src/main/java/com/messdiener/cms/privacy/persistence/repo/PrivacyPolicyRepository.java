// X:\workspace\PR_CMS\cms-feature-privacy\src\main\java\com\messdiener\cms\privacy\persistence\repo\PrivacyPolicyRepository.java
package com.messdiener.cms.privacy.persistence.repo;

import com.messdiener.cms.privacy.persistence.entity.PrivacyPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivacyPolicyRepository extends JpaRepository<PrivacyPolicyEntity, UUID> {
}
