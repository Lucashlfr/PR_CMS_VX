// src/main/java/com/messdiener/cms/liturgy/persistence/repo/LiturgieMapRepository.java
package com.messdiener.cms.liturgy.persistence.repo;

import com.messdiener.cms.liturgy.persistence.entity.LiturgieMapEntity;
import com.messdiener.cms.liturgy.persistence.entity.LiturgieMapId;
import com.messdiener.cms.shared.enums.LiturgieState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LiturgieMapRepository extends JpaRepository<LiturgieMapEntity, LiturgieMapId> {

    // Alle Mappings für ein Liturgy
    List<LiturgieMapEntity> findByIdLiturgieId(UUID liturgieId);

    // Alle Mappings für Person + Menge Liturgien
    List<LiturgieMapEntity> findByIdPersonIdAndIdLiturgieIdIn(UUID personId, Collection<UUID> liturgieIds);

    // Alle Mappings für Menge Liturgien
    List<LiturgieMapEntity> findByIdLiturgieIdIn(Collection<UUID> liturgieIds);

    // Nur „DUTY“ für ein Liturgy
    List<LiturgieMapEntity> findByIdLiturgieIdAndState(UUID liturgieId, LiturgieState state);

    // Für „Next Duty“ – alle DUTY-Mappings einer Person
    List<LiturgieMapEntity> findByIdPersonIdAndState(UUID personId, LiturgieState state);

}
