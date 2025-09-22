// .../persistence/repo/EmergencyContactRepository.java
package com.messdiener.cms.person.persistence.repo;

import com.messdiener.cms.person.persistence.entity.PersonFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PersonFlagRepository extends JpaRepository<PersonFlagEntity, UUID> {
    List<PersonFlagEntity> findByPersonIdOrderByFlagDateDesc(UUID personId);
    boolean existsByPersonIdAndFlagType(UUID personId, String flagType);
}
