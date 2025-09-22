package com.messdiener.cms.liturgy.domain.entity;

import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class LiturgieRequest {

    private UUID requestId;
    private Tenant tenant;
    private UUID creatorId;

    private int number;

    private String name;

    private CMSDate startDate;
    private CMSDate endDate;

    private CMSDate deadline;
    private boolean active;
}
