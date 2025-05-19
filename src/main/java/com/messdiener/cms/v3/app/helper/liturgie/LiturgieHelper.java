package com.messdiener.cms.v3.app.helper.liturgie;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class LiturgieHelper {

    public long getStartOfCurrentMonthMillis() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        ZonedDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone);
        return startOfMonth.toInstant().toEpochMilli();
    }

    public long getEndOfCurrentMonthMillis() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        ZonedDateTime endOfMonth = lastDay.atTime(23, 59).atZone(zone);
        return endOfMonth.toInstant().toEpochMilli();
    }


}
