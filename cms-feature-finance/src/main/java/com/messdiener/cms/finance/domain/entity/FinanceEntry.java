package com.messdiener.cms.finance.domain.entity;

import com.messdiener.cms.shared.enums.finance.TransactionCategory;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class FinanceEntry {

    private UUID id;
    private Tenant tenant;
    private UUID billId;
    private UUID userId;

    private int number;

    private CMSDate date;
    private double revenueCash;
    private double expenseCash;
    private double revenueAccount;
    private double expenseAccount;

    private String title;
    private TransactionCategory transactionCategory;
    private String notes;
}
