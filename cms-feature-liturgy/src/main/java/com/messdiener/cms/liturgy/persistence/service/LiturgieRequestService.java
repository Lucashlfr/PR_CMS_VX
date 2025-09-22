package com.messdiener.cms.liturgy.persistence.service;

import com.messdiener.cms.liturgy.domain.entity.LiturgieRequest;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieRequestEntity;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieRequestMapEntity;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieRequestMapId;
import com.messdiener.cms.liturgy.persistence.repo.LiturgieRequestMapRepository;
import com.messdiener.cms.liturgy.persistence.repo.LiturgieRequestRepository;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class LiturgieRequestService {

    private final LiturgieRequestRepository requestRepository;
    private final LiturgieRequestMapRepository requestMapRepository;
    private final PersonService personService;

    // ---- CRUD / Queries ----

    public List<LiturgieRequest> getRequestsByTenant(Tenant tenant) {
        return requestRepository.findByTenantOrderByNumberDesc(tenant)
                .stream().map(this::toDomain).toList();
    }

    public void saveRequest(LiturgieRequest req) {
        requestRepository.save(toEntity(req));
    }

    public Optional<LiturgieRequest> currentRequest(Tenant tenant) {
        return requestRepository.findTopByTenantAndActiveTrueOrderByNumberDesc(tenant).map(this::toDomain);
    }

    public void acceptRequest(UUID personId, UUID requestId) {
        LiturgieRequestMapId id = new LiturgieRequestMapId(requestId, personId);
        LiturgieRequestMapEntity e = requestMapRepository.findById(id).orElseGet(() -> {
            LiturgieRequestMapEntity x = new LiturgieRequestMapEntity();
            x.setId(id);
            return x;
        });
        e.setDate(System.currentTimeMillis());
        requestMapRepository.save(e);
    }

    public boolean sendRequest(UUID personId, UUID requestId) {
        return requestMapRepository.existsByIdRequestIdAndIdPersonId(requestId, personId);
    }

    // Für UI: „Name → hat bereits abgeschickt?“
    public Map<String, Boolean> getPersonStatusMap(Tenant tenant, UUID requestId) {
        List<PersonOverviewDTO> persons = personService.getActiveMessdienerByTenantDTO(tenant);
        Map<String, Boolean> res = new LinkedHashMap<>(persons.size());
        for (PersonOverviewDTO p : persons) {
            boolean done = requestMapRepository.existsByIdRequestIdAndIdPersonId(requestId, p.getId());
            res.put(p.getFirstName() + " " + p.getLastName(), done);
        }
        return res;
    }

    // ---- Mapper ----

    private LiturgieRequest toDomain(LiturgieRequestEntity e) {
        return new LiturgieRequest(
                e.getId(),
                e.getTenant(),
                e.getCreatorId(),
                e.getNumber(),
                e.getName(),
                CMSDate.of(e.getStartDate()),
                CMSDate.of(e.getEndDate()),
                CMSDate.of(e.getDeadline()),
                e.isActive()
        );
    }

    private LiturgieRequestEntity toEntity(LiturgieRequest d) {
        LiturgieRequestEntity e = new LiturgieRequestEntity();
        e.setId(d.getRequestId() != null ? d.getRequestId() : UUID.randomUUID());
        e.setTenant(d.getTenant());
        e.setCreatorId(d.getCreatorId());
        e.setNumber(d.getNumber());
        e.setName(d.getName());
        e.setStartDate(d.getStartDate() != null ? d.getStartDate().toLong() : 0L);
        e.setEndDate(d.getEndDate() != null ? d.getEndDate().toLong() : 0L);
        e.setDeadline(d.getDeadline() != null ? d.getDeadline().toLong() : 0L);
        e.setActive(d.isActive());
        return e;
    }
}
