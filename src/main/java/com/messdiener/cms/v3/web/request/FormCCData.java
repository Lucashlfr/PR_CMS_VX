package com.messdiener.cms.v3.web.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class FormCCData {
    private String name;
    private String description;
    private List<FormFinanceEntry> matrix;

}
