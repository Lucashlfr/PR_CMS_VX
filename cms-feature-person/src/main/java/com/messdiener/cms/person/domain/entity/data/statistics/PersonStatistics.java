package com.messdiener.cms.person.domain.entity.data.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonStatistics {

    private int max;
    private int available;
    private int duty;
    private int absent;

    private double availabilityPercentage;
    private double dutyPercentage;
    private double absentPercentage;
}
