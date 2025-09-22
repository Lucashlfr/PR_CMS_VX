// X:\workspace\PR_CMS\cms-feature-audit\src\main\java\com\messdiener\cms\audit\app\service\AuditService.java
package com.messdiener.cms.audit.persistence.service;

import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.audit.persistence.map.AuditMapper;
import com.messdiener.cms.audit.persistence.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private final AuditRepository auditRepository;

    // <— Entfernt: @PostConstruct + JDBC-DDL. Tabelle wird künftig via Migration/Flyway gepflegt. :contentReference[oaicite:5]{index=5}

    public List<AuditLog> getLogsByConnectId(UUID connectId) {
        return auditRepository.findByConnectIdOrderByTimestampDesc(connectId.toString())
                .stream().map(AuditMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:6]{index=6}
    }

    public void createLog(AuditLog auditLog) {
        auditRepository.save(AuditMapper.toEntity(auditLog)); // vormals INSERT via JDBC :contentReference[oaicite:7]{index=7}
        LOGGER.info("Audit-Log {} gespeichert (connectId={})", auditLog.getLogId(), auditLog.getConnectedId());
    }
}
