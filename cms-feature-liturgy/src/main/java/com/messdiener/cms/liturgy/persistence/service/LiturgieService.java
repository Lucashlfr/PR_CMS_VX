package com.messdiener.cms.liturgy.persistence.service;

import com.messdiener.cms.liturgy.domain.entity.Liturgie;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieEntity;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieMapEntity;
import com.messdiener.cms.liturgy.persistence.repo.LiturgieMapRepository;
import com.messdiener.cms.liturgy.persistence.repo.LiturgieRepository;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.shared.enums.LiturgieType;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.other.Pair;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiturgieService {

    private final LiturgieRepository liturgieRepository;
    private final LiturgieMapRepository liturgieMapRepository;
    private final PersonService personService;

    // ---------------- Queries / CRUD ----------------

    @Transactional(readOnly = true)
    public List<Liturgie> getLiturgies(Tenant tenant, long start, long end) {
        return liturgieRepository
                .findByTenantAndDateBetweenOrderByDateAsc(tenant, start, end)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Liturgie> getLiturgie(UUID id) {
        return liturgieRepository.findById(id).map(this::toDomain);
    }

    @Transactional
    public void save(Liturgie liturgie) {
        liturgieRepository.save(toEntity(liturgie));
    }

    // ---------------- Vom Controller/Helper erwartete Methoden ----------------

    /** Nächste Liturgien mit Pflicht-Einteilung (State=DUTY) für die angegebene Person ab 'fromTimestamp'. */
    @Transactional(readOnly = true)
    public List<Pair<Liturgie, LiturgieState>> getNextDutyLiturgies(UUID personId, long fromTimestamp) {
        List<LiturgieMapEntity> dutyMappings =
                liturgieMapRepository.findByIdPersonIdAndState(personId, LiturgieState.DUTY);
        if (dutyMappings.isEmpty()) return List.of();

        Set<UUID> liturgyIds = dutyMappings.stream()
                .map(m -> m.getId().getLiturgieId())
                .collect(Collectors.toSet());

        Map<UUID, LiturgieState> stateByLiturgy = dutyMappings.stream()
                .collect(Collectors.toMap(
                        m -> m.getId().getLiturgieId(),
                        LiturgieMapEntity::getState,
                        (a, b) -> a
                ));

        return liturgieRepository
                .findByIdInAndDateGreaterThanEqualOrderByDateAsc(liturgyIds, fromTimestamp)
                .stream()
                .map(this::toDomain)
                .map(l -> new Pair<>(l, stateByLiturgy.getOrDefault(l.getLiturgieId(), LiturgieState.UNAVAILABLE)))
                .toList();
    }

    /** Nächste Liturgien im Tenant der Person inkl. individuellem Status (Default UNAVAILABLE). */
    @Transactional(readOnly = true)
    public List<Pair<Liturgie, LiturgieState>> getNextLiturgies(Person person) {
        long now = System.currentTimeMillis();
        List<LiturgieEntity> entities =
                liturgieRepository.findByTenantAndDateGreaterThanEqualOrderByDateAsc(person.getTenant(), now);
        if (entities.isEmpty()) return List.of();

        List<UUID> liturgyIds = entities.stream().map(LiturgieEntity::getId).toList();

        Map<UUID, LiturgieState> stateByLiturgy = liturgieMapRepository
                .findByIdPersonIdAndIdLiturgieIdIn(person.getId(), liturgyIds)
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getId().getLiturgieId(),
                        LiturgieMapEntity::getState,
                        (a, b) -> a
                ));

        return entities.stream()
                .map(this::toDomain)
                .map(l -> new Pair<>(l, stateByLiturgy.getOrDefault(l.getLiturgieId(), LiturgieState.UNAVAILABLE)))
                .toList();
    }

    /**
     * Port deines bisherigen SQL-Snippets auf JPA:
     * - Holt alle DUTY-Mappings der Liturgie,
     * - lädt aktive Personen-DTOs des Tenants,
     * - filtert auf gemappte IDs,
     * - sortiert "Nachname, Vorname",
     * - gibt eine kommagetrennte Liste zurück oder "Alle die können" wenn leer.
     */
    @Transactional(readOnly = true)
    public String getDutyPersons(Liturgie liturgie) {
        // 1) Mappings (nur DUTY) laden
        List<LiturgieMapEntity> dutyMappings =
                liturgieMapRepository.findByIdLiturgieIdAndState(liturgie.getLiturgieId(), LiturgieState.DUTY);

        if (dutyMappings.isEmpty()) {
            // Entspricht der Ersatz-Logik deines Originalcodes ("#" -> "Alle die können")
            return "Alle die können";
        }

        // 2) Auf Personen-IDs reduzieren
        Set<UUID> personIds = dutyMappings.stream()
                .map(m -> m.getId().getPersonId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 3) Aktive Personen im Tenant holen und auf Mappings filtern
        List<PersonOverviewDTO> personsInTenant = personService.getActiveMessdienerByTenantDTO(liturgie.getTenant());

        List<PersonOverviewDTO> dutyPersons = personsInTenant.stream()
                .filter(p -> personIds.contains(p.getId()))
                .sorted(Comparator
                        .comparing(PersonOverviewDTO::getLastName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(PersonOverviewDTO::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        if (dutyPersons.isEmpty()) {
            return "Alle die können";
        }

        // 4) "Vorname Nachname" kommagetrennt
        return dutyPersons.stream()
                .map(p -> (p.getFirstName() != null ? p.getFirstName() : "") +
                        " " +
                        (p.getLastName() != null ? p.getLastName() : ""))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    // ---------------- Mapper ----------------

    private Liturgie toDomain(LiturgieEntity e) {
        return new Liturgie(
                e.getId(),
                e.getNumber(),
                e.getTenant(),
                e.getLiturgieType() != null ? e.getLiturgieType() : LiturgieType.WORSHIP,
                CMSDate.of(e.getDate()),
                e.isLocal()
        );
    }

    private LiturgieEntity toEntity(Liturgie d) {
        return LiturgieEntity.builder()
                .id(d.getLiturgieId() != null ? d.getLiturgieId() : UUID.randomUUID())
                .number(d.getNumber())
                .tenant(d.getTenant())
                .liturgieType(d.getLiturgieType() != null ? d.getLiturgieType() : LiturgieType.WORSHIP)
                .date(d.getDate() != null ? d.getDate().toLong() : 0L)
                .local(d.isLocal())
                .build();
    }
}
