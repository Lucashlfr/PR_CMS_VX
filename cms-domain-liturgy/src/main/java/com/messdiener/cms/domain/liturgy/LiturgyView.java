package com.messdiener.cms.domain.liturgy;

import com.messdiener.cms.utils.time.CMSDate;
import lombok.Value;

import java.util.UUID;

public record LiturgyView(
        UUID id,
        String typeLabel,
        CMSDate date,
        boolean local
) {
}