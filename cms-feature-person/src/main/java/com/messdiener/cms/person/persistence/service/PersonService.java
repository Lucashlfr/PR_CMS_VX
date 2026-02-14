package com.messdiener.cms.person.persistence.service;

import com.messdiener.cms.domain.person.PersonLoginDTO;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.map.PersonMapper;
import com.messdiener.cms.person.persistence.repo.PersonRepository;
import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.shared.enums.PersonAttributes;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository repo;

    @PostConstruct
    @Transactional
    public void init() {
        // Kein DDL mehr – Schema per Migrationstool (z. B. Flyway).
        // System-User sicherstellen (gleiches Verhalten wie zuvor).
        if (!repo.existsById(Cache.SYSTEM_USER)) {
            Person sys = Person.empty(Tenant.MSTM);
            sys.setId(Cache.SYSTEM_USER);
            sys.setActive(true);
            sys.setFirstName("System");
            updatePerson(sys);
            LOGGER.info("PersonService initialized and system user ensured.");
        }
    }

    // ------------------------------------------------------------
    // Basic CRUD / Lookups
    // ------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Person> getPersons() {
        return repo.findAll().stream().map(PersonMapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonById(UUID personId) {
        return repo.findById(personId).map(PersonMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByUsername(String username) {
        return repo.findByUsername(username).map(PersonMapper::toDomain);
    }

    @Transactional(readOnly = true)
    public List<Person> getPersonsByTenant(Tenant tenant) {
        return repo.findByTenantOrderByLastnameAsc(tenant.toString())
                .stream().map(PersonMapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public List<Person> getActiveMessdienerByTenant(Tenant tenant) {
        return repo.findByTenantAndTypeAndActiveTrueOrderByLastnameAsc(
                        tenant.toString(), PersonAttributes.Type.MESSDIENER.name())
                .stream().map(PersonMapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public String getPersonName(UUID id) {
        return repo.findById(id)
                .map(e -> e.getFirstname() + " " + e.getLastname())
                .orElse("null");
    }

    @Transactional(readOnly = true)
    public List<Person> getManagers() {
        return repo.findActiveWithMinFRank(2)
                .stream().map(PersonMapper::toDomain).toList();
    }

    @Transactional
    public void updatePerson(Person person) {
        repo.save(PersonMapper.toEntity(person)); // Upsert per @Id
    }

    // ------------------------------------------------------------
    // DTO-Varianten (Kompatibilität zu bestehenden Call-Sites)
    // ------------------------------------------------------------

    /**
     * Von Adaptern/Controllern erwartete Methode: liefert alle Logins (nur user/pass).
     */
    @Transactional(readOnly = true)
    public List<PersonLoginDTO> getPersonsByLogin() {
        return repo.findAll().stream()
                .filter(e -> e.isCanLogin())
                .map(e -> new PersonLoginDTO(e.getUsername(), e.getPassword())) // 2-arg CTOR
                .toList();
    }

    /**
     * Von PersonManagementController erwartete Methode:
     * Alle aktiven Messdiener eines Tenants als Übersicht-DTO.
     */
    @Transactional(readOnly = true)
    public List<PersonOverviewDTO> getActiveMessdienerByTenantDTO(Tenant tenant) {
        return repo.findByTenantAndTypeAndActiveTrueOrderByLastnameAsc(
                        tenant.toString(), PersonAttributes.Type.MESSDIENER.name())
                .stream()
                .map(e -> {
                    String rankName = e.getRank() != null
                            ? PersonAttributes.Rank.valueOf(e.getRank()).getName()
                            : "";

                    String birth = (e.getBirthdate() != null && e.getBirthdate() != 0)
                            ? CMSDate.of(e.getBirthdate()).getGermanDate() + " (" + CMSDate.of(e.getBirthdate()).getAge() + ")"
                            : "";
                    if (birth.startsWith("01.01.1970")) birth = ""; // Kompatibilität zu vorher

                    double[] activity = new double[3]; // Placeholder wie zuvor
                    String imgUrl = "/dist/assets/img/demo/user-placeholder.svg";

                    return new PersonOverviewDTO(
                            e.getId(),
                            e.getFirstname(),
                            e.getLastname(),
                            Tenant.valueOf(e.getTenant()),
                            rankName,
                            birth,
                            activity,
                            imgUrl,
                            e.getUsername(),
                            e.getPassword()
                    );
                })
                .toList();
    }

    /**
     * Modernisierte Variante deiner bisherigen Berechtigungssicht.
     * Beibehaltung des bisherigen Verhaltens:
     * - fRank == 3: nur aktive Messdiener im Tenant
     * - sonst: alle Personen im Tenant, gefiltert auf active && fRank >= given
     */
    @Transactional(readOnly = true)
    public List<PersonOverviewDTO> getActivePersonsByPermissionDTO(int fRank, Tenant tenant) {
        final List<Person> base;
        if (fRank == 3) {
            base = repo.findByTenantAndTypeAndActiveTrueOrderByLastnameAsc(
                            tenant.toString(), PersonAttributes.Type.MESSDIENER.name())
                    .stream().map(PersonMapper::toDomain).toList();
        } else {
            base = repo.findByTenantOrderByLastnameAsc(tenant.toString())
                    .stream().map(PersonMapper::toDomain)
                    .filter(p -> p.isActive() && p.getFRank() >= fRank)
                    .toList();
        }

        return base.stream().map(p -> {
            String rankName = p.getRank() != null ? p.getRank().getName() : "";
            String birth = p.getBirthdate().isPresent()
                    ? p.getBirthdate().get().getGermanDate() + " (" + p.getBirthdate().get().getAge() + ")"
                    : "";
            if (birth.startsWith("01.01.1970")) birth = "";

            double[] activity = new double[3];
            String imgUrl = "/dist/assets/img/demo/user-placeholder.svg";

            return new PersonOverviewDTO(
                    p.getId(),
                    p.getFirstName(),
                    p.getLastName(),
                    p.getTenant(),
                    rankName,
                    birth,
                    activity,
                    imgUrl,
                    p.getUsername(),
                    p.getPassword()
            );
        }).collect(Collectors.toList());
    }

    // ------------------------------------------------------------
    // (Optional) Hilfsmethoden/Mapper für andere Call-Sites
    // ------------------------------------------------------------

    public PersonLoginDTO toLoginDTO(Person p) {
        return new PersonLoginDTO(p.getUsername(), p.getPassword());
    }
}
