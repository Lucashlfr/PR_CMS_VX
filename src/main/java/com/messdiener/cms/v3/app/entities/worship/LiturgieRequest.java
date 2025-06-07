package com.messdiener.cms.v3.app.entities.worship;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class LiturgieRequest {

    private UUID requestId;
    private UUID tenantId;
    private UUID creatorId;

    private int number;

    private String name;

    private CMSDate startDate;
    private CMSDate endDate;

    private CMSDate deadline;
    private boolean active;
}
