// X:\workspace\PR_CMS\cms-feature-finance\src\main\java\com\messdiener\cms\finance\persistence\repo\FinanceTransactionRepository.java
package com.messdiener.cms.finance.persistence.repo;

import com.messdiener.cms.finance.persistence.entity.FinanceTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface FinanceTransactionRepository extends JpaRepository<FinanceTransactionEntity, Integer> {

    List<FinanceTransactionEntity> findByTenantOrderByDateDesc(String tenant); // SELECT ... WHERE tenant = ? ORDER BY date DESC :contentReference[oaicite:1]{index=1}

    @Query("select coalesce(sum(f.revenueCash),0) from FinanceTransactionEntity f where f.tenant = ?1")
    double sumRevenueCashByTenant(String tenant); // :contentReference[oaicite:2]{index=2}

    @Query("select coalesce(sum(f.expenseCash),0) from FinanceTransactionEntity f where f.tenant = ?1")
    double sumExpenseCashByTenant(String tenant); // :contentReference[oaicite:3]{index=3}

    @Query("select coalesce(sum(f.revenueAccount),0) from FinanceTransactionEntity f where f.tenant = ?1")
    double sumRevenueAccountByTenant(String tenant); // :contentReference[oaicite:4]{index=4}

    @Query("select coalesce(sum(f.expenseAccount),0) from FinanceTransactionEntity f where f.tenant = ?1")
    double sumExpenseAccountByTenant(String tenant); // :contentReference[oaicite:5]{index=5}
}
