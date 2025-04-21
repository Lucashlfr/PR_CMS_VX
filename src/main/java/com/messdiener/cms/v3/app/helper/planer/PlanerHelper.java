package com.messdiener.cms.v3.app.helper.planer;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.planer.PlanerEvent;
import com.messdiener.cms.v3.app.entities.planer.entities.PlanerText;
import com.messdiener.cms.v3.app.entities.planer.entities.PlannerLog;
import com.messdiener.cms.v3.app.entities.planer.tasks.PlanerTask;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.planner.PlannerEditorService;
import com.messdiener.cms.v3.app.services.planner.PlannerLogService;
import com.messdiener.cms.v3.app.services.planner.PlannerTaskService;
import com.messdiener.cms.v3.app.services.planner.PlannerTextService;
import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanerHelper {

    private final PlannerLogService plannerLogService;
    private final PlannerTaskService plannerTaskService;
    private final PlannerTextService plannerTextService;
    private final PlannerEditorService plannerEditorService;
    private final PersonService personService;

    public List<PlannerLog> getLogs(PlanerEvent event) {
        try {
            return plannerLogService.getLogs(event.getPlanerId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<PlannerLog> getLogsByTag(PlanerEvent event, String tag) {
        try {
            return plannerLogService.getLogs(event.getPlanerId(), PlanerTag.valueOf(tag));
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public String getManagerName(PlanerEvent event) {
        try {
            if (event.getManagerId().isPresent()) {
                Optional<Person> personOpt = personService.getPersonById(event.getManagerId().get());
                return personOpt.map(Person::getName).orElse("Offen");
            }
        } catch (SQLException ignored) {}
        return "Offen";
    }

    public double getProgress(PlanerEvent event) {
        try {
            return plannerTaskService.progress(event.getPlanerId());
        } catch (SQLException e) {
            return 0.0;
        }
    }

    public void saveLog(UUID planerId, PlannerLog log) {
        try {
            plannerLogService.createLog(planerId, log);
        } catch (SQLException e) {
            // Logging statt throw
        }
    }

    public void saveText(UUID planerId, PlanerText text) {
        try {
            plannerTextService.saveText(planerId, text);
        } catch (SQLException e) {
            // Logging statt throw
        }
    }

    public void saveTask(UUID planerId, PlanerTask task) {
        try {
            plannerTaskService.updateTask(planerId, task);
        } catch (SQLException e) {
            // Logging statt throw
        }
    }

    public void addEditor(UUID planerId, UUID taskId, UUID personId) {
        try {
            plannerEditorService.addEditor(planerId, taskId, personId);
        } catch (SQLException ignored) {}
    }

    public List<Person> getTaskEditors(UUID taskId) {
        try {
            return plannerEditorService.getEditors(taskId);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
