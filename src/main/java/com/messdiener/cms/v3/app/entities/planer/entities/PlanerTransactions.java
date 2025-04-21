package com.messdiener.cms.v3.app.entities.planer.entities;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PlanerTransactions {

    private UUID transactionId;
    private String transactionName;
    private String description;
    private UUID personId;
    private CMSDate date;
    private double value;

}
