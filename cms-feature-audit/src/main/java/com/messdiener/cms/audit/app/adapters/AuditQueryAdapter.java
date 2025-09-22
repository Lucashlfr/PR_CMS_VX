package com.messdiener.cms.audit.app.adapters;

import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.domain.audit.AuditLogView;
import com.messdiener.cms.domain.audit.AuditQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditQueryAdapter implements AuditQueryPort {

    private final AuditService auditService;

    @Override
    public List<AuditLogView> getLogsByConnectId(UUID connectId) {
        List<AuditLog> logs = auditService.getLogsByConnectId(connectId);
        return logs.stream()
                .map(l -> new AuditLogView(
                        l.getTimestamp() != null ? l.getTimestamp().toLong() : 0L,
                        l.getUserId() != null ? l.getUserId().toString() : "",
                        // action: kombiniere Typ/Kategorie/Titel als kompakten String
                        (l.getType() != null ? l.getType().name() : "N/A")
                                + "/" + (l.getCategory() != null ? l.getCategory().name() : "N/A")
                                + ": " + (l.getTitle() != null ? l.getTitle() : ""),
                        l.getDescription() != null ? l.getDescription() : ""
                ))
                .toList();
    }
}
