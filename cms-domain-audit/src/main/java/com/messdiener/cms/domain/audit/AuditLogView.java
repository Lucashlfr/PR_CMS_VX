package com.messdiener.cms.domain.audit;

public record AuditLogView(long ts, String user, String action, String details) {
}