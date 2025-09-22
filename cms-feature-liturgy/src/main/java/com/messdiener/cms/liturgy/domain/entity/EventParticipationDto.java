package com.messdiener.cms.liturgy.domain.entity;

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
