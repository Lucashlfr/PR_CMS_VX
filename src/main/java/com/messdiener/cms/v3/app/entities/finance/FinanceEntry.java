package com.messdiener.cms.v3.app.entities.finance;

import com.messdiener.cms.v3.shared.enums.finance.TransactionCategory;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class FinanceEntry {

    private UUID id;
    private UUID tenantId;
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
