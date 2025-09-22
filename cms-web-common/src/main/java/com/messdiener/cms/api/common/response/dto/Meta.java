package com.messdiener.cms.api.common.response.dto;

public record Meta(
        long issuedAt,
        String requestId,
        String path,
        String locale
) {}
