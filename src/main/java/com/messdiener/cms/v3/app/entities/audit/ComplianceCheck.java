package com.messdiener.cms.v3.app.entities.audit;

import com.messdiener.cms.v3.shared.enums.ComplianceType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ComplianceCheck {

    private int id;
    private ComplianceType complianceType;
    private UUID targetPerson;
    private CMSDate date;
    private boolean active;

    public static ComplianceCheck of(ComplianceType complianceType, UUID targetPerson, boolean b) {
        return new ComplianceCheck(0, complianceType, targetPerson, CMSDate.current(), b);
    }
}
