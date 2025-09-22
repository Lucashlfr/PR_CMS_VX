package com.messdiener.cms.person.persistence.service;

import com.messdiener.cms.person.domain.entity.data.flags.PersonFlag;
import com.messdiener.cms.person.persistence.map.PersonFlagMapper;
import com.messdiener.cms.person.persistence.repo.PersonFlagRepository;
import com.messdiener.cms.shared.enums.person.FlagType;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class PersonFlagService {

    private final PersonFlagRepository repo;

    public List<PersonFlag> getAllFlagsByPerson(UUID personId) {
        return repo.findByPersonIdOrderByFlagDateDesc(personId).stream().map(PersonFlagMapper::toDomain).toList();
    }

    public boolean flagExists(UUID personId, FlagType type) {
        return repo.existsByPersonIdAndFlagType(personId, type.name());
    }

    public void createDefaultFlags(UUID personId) {
        for (FlagType ft : FlagType.values()) {
            if (!ft.isDefaultCreated()) continue;
            if (flagExists(personId, ft)) continue;
            saveFlag(personId, new PersonFlag(UUID.randomUUID(), 0, ft, "/", "/", CMSDate.current(), false));
        }
    }

    public void saveFlag(UUID personId, PersonFlag flag) {
        repo.save(PersonFlagMapper.toEntity(flag, personId)); // upsert by ID
    }

    public Optional<PersonFlag> getFlag(UUID flagId) {
        return repo.findById(flagId).map(PersonFlagMapper::toDomain);
    }
}

