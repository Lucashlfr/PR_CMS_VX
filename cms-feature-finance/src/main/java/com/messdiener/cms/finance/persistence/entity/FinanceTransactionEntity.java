// X:\workspace\PR_CMS\cms-feature-finance\src\main\java\com\messdiener\cms\finance\persistence\entity\FinanceTransactionEntity.java
package com.messdiener.cms.finance.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_finance_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinanceTransactionEntity {

    // Achtung: Im Alt-Schema ist "number" AUTO_INCREMENT PRIMARY KEY.
    // Wir übernehmen das als JPA-Id, halten die fachliche UUID "id" separat (unique).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "number")
    private Integer number;

    @Column(name = "id", length = 36, nullable = false, unique = true)
    private UUID id;

    @Column(name = "tenant", length = 4, nullable = false)
    private String tenant;

    @Column(name = "billId", length = 36, nullable = false)
    private UUID billId;

    @Column(name = "userID", length = 36, nullable = false)
    private UUID userId;

    @Column(name = "date")
    private Long date; // EPOCH ms

    @Column(name = "revenueCash")
    private Double revenueCash;

    @Column(name = "expenseCash")
    private Double expenseCash;

    @Column(name = "revenueAccount")
    private Double revenueAccount;

    @Column(name = "expenseAccount")
    private Double expenseAccount;

    @Lob
    @Column(name = "title")
    private String title;

    @Column(name = "transactionCategory", length = 60)
    private String transactionCategory;

    @Lob
    @Column(name = "notes")
    private String notes;
}
