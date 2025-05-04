package com.messdiener.cms.v3.app.entities.finance;

import com.messdiener.cms.v3.shared.enums.finance.BudgetYear;
import com.messdiener.cms.v3.shared.enums.finance.CostCenter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Budget {

    private UUID id;
    private int tag;
    private UUID tenantId;
    private CostCenter costCenter;
    private BudgetYear budgetYear;

    private String name;
    private String description;
    private double plannedIncome;
    private double plannedExpenditure;

}
