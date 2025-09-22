package com.messdiener.cms.liturgy.app.adapter;

import com.messdiener.cms.domain.liturgy.LiturgyQueryPort;
import com.messdiener.cms.domain.liturgy.LiturgyView;
import com.messdiener.cms.domain.person.PersonOverviewLite;
import com.messdiener.cms.liturgy.persistence.service.LiturgieMappingService;
import com.messdiener.cms.liturgy.persistence.service.LiturgieService;
import com.messdiener.cms.liturgy.domain.entity.Liturgie;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiturgyQueryAdapter implements LiturgyQueryPort {

    private final LiturgieService liturgieService;
    private final LiturgieMappingService liturgieMappingService;

    @Override
    public List<LiturgyView> getLiturgies(Tenant tenant, long start, long end) {
        List<Liturgie> list = liturgieService.getLiturgies(tenant, start, end);
        return list.stream()
                .map(l -> new LiturgyView(
                        l.getLiturgieId(),
                        l.getLiturgieType().getLabel(),
                        l.getDate(),
                        l.isLocal()
                ))
                .toList();
    }

    @Override
    public Map<UUID, Map<UUID, LiturgieState>> getStatesForLiturgies(
            List<LiturgyView> liturgies,
            List<PersonOverviewLite> persons
    ) {
        // 1) LiturgyView -> Liturgie (minimal) für MappingService
        List<Liturgie> lits = liturgies.stream()
                .map(v -> new Liturgie(
                        v.getId(),   // liturgieId
                        0,           // number (für State-Abfrage irrelevant)
                        null,        // tenant (nicht benötigt)
                        null,        // type (nicht benötigt)
                        v.getDate(), // CMSDate
                        v.isLocal()  // local
                ))
                .toList();

        // 2) PersonOverviewLite -> PersonOverviewDTO
        List<PersonOverviewDTO> personDtos = persons.stream()
                .map(p -> new PersonOverviewDTO(
                        p.id(),
                        p.firstName(),
                        p.lastName(),
                        p.tenant(),
                        null, null, null, null, null, null
                ))
                .toList();

        // 3) MappingService liefert bereits die gewünschte Struktur:
        return liturgieMappingService.getStatesForLiturgies(lits, personDtos);

    }
}
