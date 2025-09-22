package com.messdiener.cms.domain.audit;
import java.util.*;
import java.util.UUID;

public interface AuditQueryPort {
    List<AuditLogView> getLogsByConnectId(UUID connectId);
}