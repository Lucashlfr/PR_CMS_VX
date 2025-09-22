// X:\workspace\PR_CMS\cms-feature-finance\src\main\java\com\messdiener\cms\finance\persistence\map\FinanceTransactionMapper.java
package com.messdiener.cms.finance.persistence.map;

import com.messdiener.cms.finance.domain.entity.FinanceEntry;
import com.messdiener.cms.finance.persistence.entity.FinanceTransactionEntity;
import com.messdiener.cms.shared.enums.finance.TransactionCategory;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;

public final class FinanceTransactionMapper {

    private FinanceTransactionMapper(){}

    public static FinanceEntry toDomain(FinanceTransactionEntity e) {
        return new FinanceEntry(
                e.getId(),
                Tenant.valueOf(e.getTenant()),
                e.getBillId(),
                e.getUserId(),
                e.getNumber() != null ? e.getNumber() : 0,
                new CMSDate(e.getDate() != null ? e.getDate() : 0L),
                nz(e.getRevenueCash()),
                nz(e.getExpenseCash()),
                nz(e.getRevenueAccount()),
                nz(e.getExpenseAccount()),
                e.getTitle(),
                TransactionCategory.valueOf(e.getTransactionCategory()),
                e.getNotes()
        );
    }

    public static FinanceTransactionEntity toEntity(FinanceEntry d) {
        return FinanceTransactionEntity.builder()
                .number(d.getNumber() == 0 ? null : d.getNumber()) // Auto-Increment, wenn 0
                .id(d.getId())
                .tenant(d.getTenant().toString())
                .billId(d.getBillId())
                .userId(d.getUserId())
                .date(d.getDate() != null ? d.getDate().getDate() : System.currentTimeMillis())
                .revenueCash(d.getRevenueCash())
                .expenseCash(d.getExpenseCash())
                .revenueAccount(d.getRevenueAccount())
                .expenseAccount(d.getExpenseAccount())
                .title(d.getTitle())
                .transactionCategory(d.getTransactionCategory().toString())
                .notes(d.getNotes())
                .build();
    }

    private static double nz(Double v){ return v == null ? 0.0 : v; }
}
