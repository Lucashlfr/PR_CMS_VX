package com.messdiener.cms.v3.app.entities.finance;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Receipt {

    private UUID id;
    private int tag;
    private UUID transactionId;
    private String description;
    private CMSDate date;
    private String path;
    private double amount;
}
