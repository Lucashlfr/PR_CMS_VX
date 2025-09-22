// .../persistence/repo/EmergencyContactRepository.java
package com.messdiener.cms.person.persistence.repo;

import com.messdiener.cms.person.persistence.entity.PersonConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PersonConnectionRepository extends JpaRepository<PersonConnectionEntity, UUID> {
    List<PersonConnectionEntity> findByHost(UUID host);
}
