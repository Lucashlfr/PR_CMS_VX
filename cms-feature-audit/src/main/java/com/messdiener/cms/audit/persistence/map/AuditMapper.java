// X:\workspace\PR_CMS\cms-feature-audit\src\main\java\com\messdiener\cms\audit\persistence\map\AuditMapper.java
package com.messdiener.cms.audit.persistence.map;

import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.audit.persistence.entity.AuditLogEntity;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

public final class AuditMapper {

    private AuditMapper(){}

    public static AuditLog toDomain(AuditLogEntity e){
        return new AuditLog(
                e.getLogId(),
                e.getType(),
                e.getCategory(),
                // Domain erwartet UUID; DB hält VARCHAR(255).
                safeUuid(e.getConnectId()),
                e.getUserId(),
                CMSDate.of(e.getTimestamp() == null ? 0L : e.getTimestamp()),
                e.getTitle(),
                e.getDescription(),
                e.getMic(),
                Boolean.TRUE.equals(e.getFile())
        );
    }

    public static AuditLogEntity toEntity(AuditLog d){
        return AuditLogEntity.builder()
                .logId(d.getLogId())
                .type(d.getType())
                .category(d.getCategory())
                .connectId(d.getConnectedId() != null ? d.getConnectedId().toString() : "")
                .userId(d.getUserId())
                .timestamp(d.getTimestamp() != null ? d.getTimestamp().toLong() : System.currentTimeMillis())
                .title(d.getTitle())
                .description(d.getDescription())
                .mic(d.getMic())
                .file(d.isFile())
                .build();
    }

    private static UUID safeUuid(String s){
        try { return UUID.fromString(s); } catch (Exception ex){ return null; }
    }
}
