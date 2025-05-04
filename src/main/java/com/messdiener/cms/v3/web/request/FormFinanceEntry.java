package com.messdiener.cms.v3.web.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FormFinanceEntry {

    private int year;
    private double plannedIncome;
    private double plannedExpenditure;

}
