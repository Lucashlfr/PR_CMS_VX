package com.messdiener.cms.liturgy.persistence.service;

import com.messdiener.cms.liturgy.domain.entity.Liturgie;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieMapEntity;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieMapId;
import com.messdiener.cms.liturgy.persistence.repo.LiturgieMapRepository;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.other.Pair;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LiturgieMappingService {

    private final LiturgieMapRepository liturgieMapRepository;
    private final PersonService personService;

    // Einzelner State
    public LiturgieState getState(UUID liturgieId, UUID personId) {
        return liturgieMapRepository.findById(new LiturgieMapId(liturgieId, personId))
                .map(LiturgieMapEntity::getState)
                .orElse(LiturgieState.UNAVAILABLE);
    }

    // State setzen (upssert)
    public void setState(UUID liturgieId, UUID personId, LiturgieState state) {
        LiturgieMapId id = new LiturgieMapId(liturgieId, personId);
        LiturgieMapEntity entity = liturgieMapRepository.findById(id)
                .orElseGet(() -> {
                    LiturgieMapEntity e = new LiturgieMapEntity();
                    e.setId(id);
                    return e;
                });
        entity.setState(state);
        liturgieMapRepository.save(entity);
    }

    // Liste für ein Liturgy (für API „/availability“)
    public List<Pair<PersonOverviewDTO, LiturgieState>> getStateForLiturgy(Tenant tenant, UUID liturgieId) {
        List<PersonOverviewDTO> persons = personService.getActiveMessdienerByTenantDTO(tenant);
        if (persons.isEmpty()) return List.of();

        Map<UUID, LiturgieState> stateByPerson = liturgieMapRepository.findByIdLiturgieId(liturgieId)
                .stream()
                .collect(Collectors.toMap(e -> e.getId().getPersonId(), LiturgieMapEntity::getState, (a,b)->a));

        return persons.stream()
                .map(p -> new Pair<>(p, stateByPerson.getOrDefault(p.getId(), LiturgieState.UNAVAILABLE)))
                .toList();
    }

    // Bulk: States für mehrere Liturgien & Personen (für Overview/Export)
    public Map<UUID, Map<UUID, LiturgieState>> getStatesForLiturgies(List<Liturgie> liturgieList, List<PersonOverviewDTO> persons) {
        if (liturgieList.isEmpty() || persons.isEmpty()) return Collections.emptyMap();

        List<UUID> liturgyIds = liturgieList.stream().map(Liturgie::getLiturgieId).toList();
        Set<UUID> personIds = persons.stream().map(PersonOverviewDTO::getId).collect(Collectors.toSet());

        // Alle Mappings zu den Liturgien holen
        Map<UUID, Map<UUID, LiturgieState>> result = new HashMap<>();
        liturgyIds.forEach(id -> result.put(id, new HashMap<>()));
        liturgieMapRepository.findByIdLiturgieIdIn(liturgyIds)
                .forEach(e -> {
                    UUID lId = e.getId().getLiturgieId();
                    UUID pId = e.getId().getPersonId();
                    if (personIds.contains(pId)) {
                        result.get(lId).put(pId, e.getState());
                    }
                });

        // Defaults auf UNAVAILABLE setzen
        for (UUID lId : liturgyIds) {
            Map<UUID, LiturgieState> inner = result.get(lId);
            for (UUID pId : personIds) {
                inner.putIfAbsent(pId, LiturgieState.UNAVAILABLE);
            }
        }
        return result;
    }

    public void unassignWithReason(UUID personId, UUID liturgieId, String reason) {
        // Grund aktuell nur „CANCELED“ – Reason könntest du in eigener Tabelle auditieren.
        setState(liturgieId, personId, LiturgieState.CANCELED);
    }
}
