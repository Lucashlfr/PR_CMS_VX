package com.messdiener.cms.api.common.response;

import com.messdiener.cms.api.common.response.dto.Meta;

public record ApiResponse<T>(
        T data,
        Meta meta
) {}
