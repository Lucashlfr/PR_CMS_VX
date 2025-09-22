package com.messdiener.cms.person.persistence.repo;

import com.messdiener.cms.person.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {

    Optional<PersonEntity> findByUsername(String username);

    List<PersonEntity> findByTenantOrderByLastnameAsc(String tenant);

    List<PersonEntity> findByTenantAndTypeAndActiveTrueOrderByLastnameAsc(String tenant, String type);

    // Explizit, um das Feld 'fRank' sicher anzusprechen (Edge-Case bei abgeleiteten Namen).
    @Query("select p from PersonEntity p where p.active = true and p.fRank >= :min")
    List<PersonEntity> findActiveWithMinFRank(@Param("min") int min);
}
