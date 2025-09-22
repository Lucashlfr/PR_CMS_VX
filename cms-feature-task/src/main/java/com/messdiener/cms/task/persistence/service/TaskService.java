// X:\workspace\PR_CMS\cms-feature-task\src\main\java\com\messdiener\cms\task\app\service\taskService\TaskService.java
package com.messdiener.cms.task.persistence.service;

import com.messdiener.cms.task.domain.entity.task.Task;
import com.messdiener.cms.task.persistence.map.TaskMapper;
import com.messdiener.cms.task.persistence.repo.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    // --- JDBC-@PostConstruct (CREATE TABLE) entfernt — übernimmt künftig Migration/Flyway. :contentReference[oaicite:7]{index=7}

    public void saveTask(Task task) {
        // Altverhalten: vorherigen Datensatz zu taskId löschen, dann (re)insert. :contentReference[oaicite:8]{index=8}
        taskRepository.deleteByTaskId(task.getTaskId());
        taskRepository.save(TaskMapper.toEntity(task));
        LOGGER.info("Saved task {} (number={})", task.getTaskId(), task.getNumber());
    }

    public List<Task> getTasksByLink(UUID link) {
        return taskRepository.findByLinkOrderByNumberAsc(link).stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findByTaskId(id).map(TaskMapper::toDomain);
    }
}
