package com.messdiener.cms.liturgy.app.adapter;

import com.messdiener.cms.domain.liturgy.LiturgyHelperPort;
import com.messdiener.cms.liturgy.app.helper.LiturgieHelper;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiturgyHelperAdapter implements LiturgyHelperPort {

    private final LiturgieHelper liturgieHelper;   // deine bestehende Klasse
    private final PersonService personService;

    @Override
    public void loadOverview(Model model,
                             Optional<String> startDateS,
                             Optional<String> endDateS,
                             Tenant tenantOfViewer,
                             Optional<UUID> targetPersonId) {

        try {
            // viewer = irgendeine Person im gleichen Tenant (wichtig für Tenant-Filter der Helper-Methode)
            // Falls target gesetzt ist, laden wir auch die Zielperson als Person.
            // Für viewer nehmen wir SYSTEM_USER als Fallback (Tenant kommt über Parameter).
            Person viewer = Person.empty(tenantOfViewer);

            Optional<Person> target = targetPersonId
                    .flatMap(personService::getPersonById);

            liturgieHelper.extractedLoadMethod(model, startDateS, endDateS, viewer, target);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load liturgy overview", e);
        }
    }

    @Override
    public java.util.List<com.messdiener.cms.domain.liturgy.EventParticipationDto> getParticipation(
            UUID tenant, Optional<String> start, Optional<String> end, Optional<UUID> onlyPerson) {
        throw new UnsupportedOperationException("Provide when needed or adapt to your existing code.");
    }
}
