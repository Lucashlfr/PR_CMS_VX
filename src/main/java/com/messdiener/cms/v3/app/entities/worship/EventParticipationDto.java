package com.messdiener.cms.v3.app.entities.worship;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EventParticipationDto {
    private UUID liturgieId;
    private String date;
    private long dutyCount;
    private long availableCount;
    private long unavailableCount;

}
