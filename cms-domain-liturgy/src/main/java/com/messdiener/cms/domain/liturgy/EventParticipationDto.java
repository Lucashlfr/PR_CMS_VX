package com.messdiener.cms.domain.liturgy;

import lombok.Value;

import java.util.UUID;

@Value
public class EventParticipationDto {
    UUID liturgyId;
    String date;
    long duty;
    long available;
    long unavailable;
}
