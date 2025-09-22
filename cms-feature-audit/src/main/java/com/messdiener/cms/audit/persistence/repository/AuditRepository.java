// X:\workspace\PR_CMS\cms-feature-audit\src\main\java\com\messdiener\cms\audit\app\repository\AuditRepository.java
package com.messdiener.cms.audit.persistence.repository;

import com.messdiener.cms.audit.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditRepository extends JpaRepository<AuditLogEntity, UUID> {

    // SELECT * FROM module_audit WHERE connectId=? ORDER BY timestamp DESC
    List<AuditLogEntity> findByConnectIdOrderByTimestampDesc(String connectId); // :contentReference[oaicite:4]{index=4}
}
