package com.messdiener.cms.v3.shared.scheduler;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.services.person.PersonLoginService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.tasks.TaskMessageService;
import com.messdiener.cms.v3.app.services.tasks.TaskQueryService;
import com.messdiener.cms.v3.app.services.tasks.TaskService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowQueryService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.shared.enums.tasks.*;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GlobalManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalManager.class);

    private final PersonService personService;
    private final TaskService taskService;
    private final TaskQueryService taskQueryService;
    private final WorkflowService workflowService;
    private final WorkflowQueryService workflowQueryService;
    private final TaskMessageService taskMessageService;
    private final PersonLoginService personLoginService;

    private final UUID systemUserId = UUID.fromString("93dacda6-b951-413a-96dc-9a37858abe3e");

    private final TaskMessage taskMessage = new TaskMessage(
            UUID.randomUUID(),
            MessageType.START,
            "Der Task wurde über den GlobalManager erstellt.",
            "Der Task wurde über den GlobalManager generiert",
            MessageInformationCascade.C0,
            CMSDate.current(),
            Optional.of(systemUserId),
            false
    );

    @PostConstruct
    public void logInit() {
        LOGGER.info("GlobalManager initialized.");
    }

    public void startUp() throws SQLException {
        personLoginService.matchPersonToUser(); // <- hier aufrufen
        for (Person person : personService.getPersons()) {
            if (!person.isActive()) continue;

            createTaskIfNotExists(person, "OB1", "Onboarding: Persönliche Informationen",
                    "Zu Beginn benötigen wir einige Angaben zu deiner Person wie Name, Adresse und Geburtsdatum.", TaskCategory.ONBOARDING);

            createTaskIfNotExists(person, "OB2", "Onboarding: Notfallkontakt",
                    "Bitte hinterlege einen oder mehrere Notfallkontakte. Das können am besten deine Eltern sein..", TaskCategory.ONBOARDING);

            createTaskIfNotExists(person, "OB3", "Onboarding: Datenschutz",
                    "Bitte bestätige, dass du mit der Verarbeitung deiner Daten gemäß der DSGVO einverstanden bist..", TaskCategory.ONBOARDING);

            createTaskIfNotExists(person, "OB4", "Onboarding: Selbstauskunftserklärung",
                    "Wer in der katholischen Kirche Verantwortung übernimmt, muss eine Selbstaufklärung unterschreiben.", TaskCategory.ONBOARDING);

            for (Workflow workflow : workflowQueryService.getWorkflowsByUser(person.getId())) {
                if (workflow.getWorkflowState() == WorkflowAttributes.WorkflowState.COMPLETED) continue;
                String key = "WF_" + workflow.getCreationDate().getDate();
                if (taskQueryService.normedTaskExists(person.getId(), key)) continue;

                Task task = new Task(UUID.randomUUID(), 0, key,
                        "Workflow: " + workflow.getWorkflowType().getLabel() + " (" + workflow.getCreationDate().getGermanDate() + ")",
                        "Bitte bearbeite den Workflow.",
                        Optional.of(systemUserId), Optional.of(person.getId()), Optional.empty(), Optional.empty(), Optional.empty(),
                        TaskType.JOB, TaskCategory.WORKFLOW, Optional.of(person.getId()), TaskState.REDIRECTED,
                        CMSDate.current(), CMSDate.current(), CMSDate.current(), Optional.empty(), "/workflows", "/workflows",
                        TaskPriority.P1, Optional.empty(), new ArrayList<>());
                taskService.saveTask(task);
                taskMessageService.saveMessage(task.getTaskId(), taskMessage);
            }

            createTaskIfNotExists(person, "migration", "Migration",
                    "Der Benutzer wurde ins neue System migriert.", TaskCategory.NULL, TaskState.COMPLETED, "/", Optional.of(CMSDate.current()));
        }
    }

    private void createTaskIfNotExists(Person person, String key, String title, String description, TaskCategory category) throws SQLException {
        createTaskIfNotExists(person, key, title, description, category, TaskState.REDIRECTED, "/onboarding", Optional.empty());
    }

    private void createTaskIfNotExists(Person person, String key, String title, String description, TaskCategory category, TaskState state, String route, Optional<CMSDate> doneDate) throws SQLException {
        if (taskQueryService.normedTaskExists(person.getId(), key)) return;

        Task task = new Task(UUID.randomUUID(), 0, key,
                title,
                description,
                Optional.of(systemUserId), Optional.of(person.getId()), Optional.empty(), Optional.empty(), Optional.empty(),
                TaskType.JOB, category, Optional.of(person.getId()), state,
                CMSDate.current(), CMSDate.current(), CMSDate.current(), doneDate,
                route, route,
                TaskPriority.P1, Optional.empty(), new ArrayList<>());
        taskService.saveTask(task);
        taskMessageService.saveMessage(task.getTaskId(), taskMessage);
    }
}
