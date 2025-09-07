package com.messdiener.cms.v3.api.events.service;


import com.messdiener.cms.v3.api.events.dto.AppointmentDto;

import java.util.List;

public interface AppointmentsService {

    /**
     * Liefert kommende Termine, bereits als DTO gemappt.
     * Implementierung kann DB/Repository verwenden.
     */
    List<AppointmentDto> findUpcoming(int limit);
}
