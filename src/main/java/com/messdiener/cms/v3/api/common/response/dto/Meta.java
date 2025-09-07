package com.messdiener.cms.v3.api.common.response.dto;

public record Meta(
        long issuedAt,
        String requestId,
        String path,
        String locale
) {}
