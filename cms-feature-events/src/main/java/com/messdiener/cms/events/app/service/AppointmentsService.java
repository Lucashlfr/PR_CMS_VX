package com.messdiener.cms.events.app.service;

import com.messdiener.cms.events.api.dto.AppointmentDto;

import java.util.List;

public interface AppointmentsService {

    /**
     * Liefert kommende Termine, bereits als DTO gemappt.
     * Implementierung kann DB/Repository verwenden.
     */
    List<AppointmentDto> findUpcoming(int limit);
}
