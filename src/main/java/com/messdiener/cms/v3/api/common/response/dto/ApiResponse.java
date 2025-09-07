package com.messdiener.cms.v3.api.common.response.dto;

public record ApiResponse<T>(
        T data,
        Meta meta
) {}
