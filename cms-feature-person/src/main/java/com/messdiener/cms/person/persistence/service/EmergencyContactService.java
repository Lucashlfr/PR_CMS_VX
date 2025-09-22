package com.messdiener.cms.person.persistence.service;

import com.messdiener.cms.person.domain.entity.data.EmergencyContact;
import com.messdiener.cms.person.persistence.map.EmergencyContactMapper;
import com.messdiener.cms.person.persistence.repo.EmergencyContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository repo;

    public void saveEmergencyContact(UUID personId, EmergencyContact contact) {
        repo.save(EmergencyContactMapper.toEntity(contact, personId));
    }

    public List<EmergencyContact> getEmergencyContactsByPerson(UUID personId) {
        return repo.findByPersonIdAndActiveTrueOrderByLastNameAscFirstNameAsc(personId)
                .stream().map(EmergencyContactMapper::toDomain).toList();
    }

    public void deleteEmergencyContact(UUID id) {
        repo.findById(id).ifPresent(e -> { e.setActive(false); repo.save(e); });
    }

    public void deleteEmergencyContactsByUser(UUID personId) {
        // Soft-Delete: alle auf inactive setzen
        repo.findByPersonIdAndActiveTrueOrderByLastNameAscFirstNameAsc(personId).forEach(e -> { e.setActive(false); });
        repo.flush();
    }
}