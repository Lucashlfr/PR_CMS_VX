// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\app\service\PlannerTaskService.java
package com.messdiener.cms.events.app.service;

import com.messdiener.cms.events.domain.entity.PlanerTask;
import com.messdiener.cms.events.persistence.entity.PlanerTaskEntity;
import com.messdiener.cms.events.persistence.map.PlanerTaskMapper;
import com.messdiener.cms.events.persistence.repo.PlanerTaskRepository;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannerTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerTaskService.class);

    private final PlanerTaskRepository repo;

    // --- API wie vorher (Controller & Helper rufen diese Signaturen auf) ---

    public void updateTask(UUID eventId, PlanerTask planerTask) {
        // Semantik wie bisher: Datensatz für taskId löschen/ersetzen → save() überschreibt.
        PlanerTaskEntity entity = PlanerTaskMapper.toEntity(eventId, planerTask);
        repo.save(entity); // vormals INSERT nach delete, jetzt upsert per ID. :contentReference[oaicite:1]{index=1}
        LOGGER.info("Updated task '{}' in planer '{}'.", planerTask.getTaskId(), eventId);
    }

    public List<PlanerTask> getTasks(UUID planerId) {
        return repo.findByPlanerIdOrderByTaskNumberAsc(planerId)
                .stream().map(PlanerTaskMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:2]{index=2}
    }

    public Optional<PlanerTask> getTaskById(UUID taskId) {
        return repo.findById(taskId).map(PlanerTaskMapper::toDomain); // :contentReference[oaicite:3]{index=3}
    }

    public double progress(UUID planerId) {
        List<PlanerTask> tasks = getTasks(planerId); // gleiche Logik wie vorher
        if (tasks.isEmpty()) return 0d;
        long completed = tasks.stream().filter(t -> t.getState() == CMSState.COMPLETED).count(); // :contentReference[oaicite:4]{index=4}
        return (completed * 100.0) / tasks.size();
    }
}
