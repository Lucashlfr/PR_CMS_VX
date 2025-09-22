// X:\workspace\PR_CMS\cms-feature-auth\src\main\java\com\messdiener\cms\auth\persistence\repo\PersistentLoginRepository.java
package com.messdiener.cms.auth.persistence.repo;

import com.messdiener.cms.auth.persistence.entity.PersistentLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersistentLoginRepository extends JpaRepository<PersistentLoginEntity, String> {

    Optional<PersistentLoginEntity> findBySeries(String series);

    void deleteByUsername(String username);
}
