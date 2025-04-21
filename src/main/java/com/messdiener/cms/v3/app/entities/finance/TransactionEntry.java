package com.messdiener.cms.v3.app.entities.finance;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransactionEntry {

    private UUID id;
    private UUID tenant;
    private UUID creator;
    private String type;

    private CMSDate documentDate;

    private String costCenter;
    private String description;
    private String note;

    private double value;

    private boolean exchange;

    private boolean active;

    private Optional<UUID> planerId;

    public static TransactionEntry empty(UUID tenantId, UUID personId) {
        return new TransactionEntry(UUID.randomUUID(), tenantId, personId, "", CMSDate.current(), "", "", "", 0d, false, true, Optional.empty());
    }

    public String getValueFormatted() {
        return String.format("%.2f", value).replace('.', ',') + " €";
    }
}
