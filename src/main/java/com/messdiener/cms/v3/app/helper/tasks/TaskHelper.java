package com.messdiener.cms.v3.app.helper.tasks;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.tasks.TaskMessageService;
import com.messdiener.cms.v3.app.services.tasks.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskHelper {

    private final TaskService taskService;
    private final TaskMessageService messageService;
    private final TaskMessageService taskLogService;
    private final PersonService personService;

    public Optional<Person> getCreator(Task task) {
        return task.getCreatorUserId().flatMap(id -> {
            try {
                return personService.getPersonById(id);
            } catch (SQLException e) {
                return Optional.empty();
            }
        });
    }

    public Optional<Person> getProcessor(Task task) {
        return task.getProcessorUserId().flatMap(id -> {
            try {
                return personService.getPersonById(id);
            } catch (SQLException e) {
                return Optional.empty();
            }
        });
    }

    public Optional<String> getProcessorName(Task task) {
        return task.getProcessorUserId().flatMap(id -> {
            try {
                return personService.getPersonById(id).map(Person::getName);
            } catch (SQLException e) {
                // Optional: Logger nutzen
                // logger.error("Fehler beim Abrufen des Namens für Person-ID: " + id, e);
                return Optional.empty();
            }
        });
    }

    public Optional<Person> getResponsible(Task task) {
        return task.getResponsibleUserId().flatMap(id -> {
            try {
                return personService.getPersonById(id);
            } catch (SQLException e) {
                return Optional.empty();
            }
        });
    }

    public boolean saveTask(Task task) {
        try {
            taskService.saveTask(task);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addMessage(Task task, TaskMessage message) {
        try {
            messageService.saveMessage(task.getTaskId(), message);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean createGenericMessage(Task task, TaskMessage message) {
        try {
            message.setMessageId(UUID.randomUUID());
            messageService.saveMessage(task.getTaskId(), message);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Optional<Person> getMessageSender(TaskMessage message) {
        return message.getCreatorId().flatMap(id -> {
            try {
                return personService.getPersonById(id);
            } catch (SQLException e) {
                return Optional.empty();
            }
        });
    }

    public List<TaskMessage> getLogs(Task task) {
        try {
            return taskLogService.getMessagesByTask(task.getTaskId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
