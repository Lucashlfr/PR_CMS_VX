package com.messdiener.cms.v3.api.events.service.impl;

import com.messdiener.cms.v3.api.events.dto.AppointmentDto;
import com.messdiener.cms.v3.api.events.service.AppointmentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Platzhalter-Implementierung.
 * TODO: An deine bestehende Datenquelle (Repository/Service) anbinden und AppointmentDto befüllen.
 */
@Service
@RequiredArgsConstructor
public class AppointmentsServiceImpl implements AppointmentsService {

    // Beispiel: injiziere hier deinen Repository/Service
    // private final AppointmentRepository repo;

    @Override
    public List<AppointmentDto> findUpcoming(int limit) {
        // TODO: Echte Daten liefern.
        // return repo.findUpcoming(limit).stream().map(this::toDto).toList();
        return Collections.emptyList();
    }

    // Beispiel-Mapping – an dein Domainmodel anpassen:
    // private AppointmentDto toDto(Appointment a) {
    //     return new AppointmentDto(
    //         a.getId().toString(),
    //         a.getTitle(),
    //         a.getDate().toString(), // oder formatiert
    //         a.getTime().toString(), // oder formatiert
    //         a.getLocation(),
    //         a.getDescription()
    //     );
    // }
}
