package com.messdiener.cms.v3.app.entities.finance;

import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.finance.TransactionState;
import com.messdiener.cms.v3.shared.enums.finance.TransactionType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Transaction {

    private UUID id;
    private int tag;
    private UUID tenantId;
    private UUID userId;
    private UUID budgetId;

    private CMSDate date;

    private TransactionState transactionState;
    private UUID targetUserId;

    private String title;
    private String description;
    private TransactionType type;


    public static Transaction empty(UUID tenantId, UUID userId) {
        return new Transaction(UUID.randomUUID(), 0, tenantId, userId, UUID.randomUUID(), CMSDate.current(), TransactionState.CREATED, Cache.SYSTEM_USER, "","", TransactionType.ACCOUNT);
    }
}
