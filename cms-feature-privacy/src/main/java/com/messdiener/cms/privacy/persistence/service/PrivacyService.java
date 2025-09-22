// X:\workspace\PR_CMS\cms-feature-privacy\src\main\java\com\messdiener\cms\privacy\app\service\PrivacyService.java
package com.messdiener.cms.privacy.persistence.service;

import com.messdiener.cms.privacy.domain.entity.PrivacyPolicy;
import com.messdiener.cms.privacy.persistence.map.PrivacyPolicyMapper;
import com.messdiener.cms.privacy.persistence.repo.PrivacyPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA-Refactor des bisherigen JDBC-Services:
 * - Beibehalt der öffentlichen Methoden: create(..), getAll(), getById(..)  (von Adaptern/Controllern genutzt).
 * - Entferntes @PostConstruct-DDL; Schema via Migration pflegen. :contentReference[oaicite:3]{index=3}
 */
@Service
@RequiredArgsConstructor
public class PrivacyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyService.class);

    private final PrivacyPolicyRepository repository;

    public void create(PrivacyPolicy privacyPolicy) {
        // Altverhalten: delete + insert (per DatabaseService.delete(...)) → hier durch save(..) abgebildet. :contentReference[oaicite:4]{index=4}
        repository.save(PrivacyPolicyMapper.toEntity(privacyPolicy));
        LOGGER.info("Privacy policy '{}' gespeichert.", privacyPolicy.getId());
    }

    public List<PrivacyPolicy> getAll() {
        return repository.findAll().stream()
                .map(PrivacyPolicyMapper::toDomain)
                .collect(Collectors.toList()); // ALT: SELECT * FROM module_privacy_policy :contentReference[oaicite:5]{index=5}
    }

    public Optional<PrivacyPolicy> getById(UUID id) {
        return repository.findById(id).map(PrivacyPolicyMapper::toDomain); // ALT: WHERE id=? :contentReference[oaicite:6]{index=6}
    }
}
