// .../persistence/repo/EmergencyContactRepository.java
package com.messdiener.cms.person.persistence.repo;

import com.messdiener.cms.person.persistence.entity.EmergencyContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContactEntity, UUID> {
    List<EmergencyContactEntity> findByPersonIdAndActiveTrueOrderByLastNameAscFirstNameAsc(UUID personId);
    void deleteByPersonId(UUID personId); // für „hartes“ Delete, wenn nötig
}
