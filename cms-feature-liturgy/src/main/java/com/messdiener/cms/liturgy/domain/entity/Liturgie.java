package com.messdiener.cms.liturgy.domain.entity;

import com.messdiener.cms.shared.enums.LiturgieType;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Liturgie {

    private UUID liturgieId;
    private int number;
    private Tenant tenant;

    private LiturgieType liturgieType;
    private CMSDate date;
    private boolean local;
}
