// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\app\service\PlannerMappingService.java
package com.messdiener.cms.events.app.service;

import com.messdiener.cms.events.persistence.entity.PlanerMapEntity;
import com.messdiener.cms.events.persistence.entity.PlanerPrincipalEntity;
import com.messdiener.cms.events.persistence.entity.id.PlanerMapId;
import com.messdiener.cms.events.persistence.entity.id.PlanerPrincipalId;
import com.messdiener.cms.events.persistence.repo.PlanerMapRepository;
import com.messdiener.cms.events.persistence.repo.PlanerPrincipalRepository;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerMappingService {

    private final PlanerPrincipalRepository principalRepo;
    private final PlanerMapRepository mapRepo;
    private final PersonService personService;

    /** Vom PlannerController erwartet: Clear & Insert aller Principals für ein Event. */ // :contentReference[oaicite:6]{index=6}
    public void savePrincipal(UUID planerId, List<UUID> userIds) {
        principalRepo.deleteById_PlanerId(planerId);
        List<PlanerPrincipalEntity> list = userIds.stream()
                .map(uid -> PlanerPrincipalEntity.builder().id(new PlanerPrincipalId(planerId, uid)).build())
                .collect(Collectors.toList());
        principalRepo.saveAll(list);
    }

    public List<Person> getPrincipals(UUID planerId) {
        List<UUID> userIds = principalRepo.findById_PlanerId(planerId).stream()
                .map(e -> e.getId().getUserId()).toList();
        return userIds.stream()
                .map(personService::getPersonById) // Optional<Person>
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(Person::getLastName))
                .toList();
    }

    public void mapUser(UUID planerId, UUID userId){
        mapRepo.save(PlanerMapEntity.builder()
                .id(new PlanerMapId(planerId, userId))
                .date(System.currentTimeMillis())
                .build());
    }

    public void unmapUser(UUID planerId, UUID userId){
        mapRepo.deleteById(new PlanerMapId(planerId, userId));
    }

    public List<UUID> getMappedUserIds(UUID planerId){
        return mapRepo.findById_PlanerIdOrderById_UserIdAsc(planerId).stream()
                .map(e -> e.getId().getUserId())
                .toList();
    }
}
