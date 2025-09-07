package com.messdiener.cms.v3.app.entities.worship;

import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.shared.enums.tenant.Tenant;
import com.messdiener.cms.v3.utils.time.CMSDate;
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
