package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.helper.tasks.TaskHelper;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.tasks.*;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TasksController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksController.class);
    private final Cache cache;
    private final TaskHelper taskHelper;
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("TasksController initialized.");
    }

    @GetMapping("/tasks")
    public String tasks(HttpSession session, Model model, @RequestParam Optional<String> q, @RequestParam Optional<String> id) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(session);

        if (!personHelper.hasPermission(user, "TEAM")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");
        }
//TODO; FIX
        model.addAttribute("persons", cache.getPersonService().getActivePersonsByPermission(user.getId(), "*", user.getTenantId()));
        model.addAttribute("types", TaskType.values());

        List<Task> tasks = cache.getTaskQueryService().getTaskByUserId(user.getId());

        // Optional: alle Tasks anzeigen
        // if (q.isPresent() && q.get().equals("all")) {
        //     tasks = cache.getTaskService().getAllTasksByTenant(user.getTenantId());
        // }

        model.addAttribute("tasks", tasks);

        if (id.isPresent()) {
            UUID taskId = UUID.fromString(id.get());
            Task task = cache.getTaskService().getTaskById(taskId).orElseThrow(() -> {
                LOGGER.warn("Task not found: {}", taskId);
                return new ResponseStatusException(HttpStatus.NOT_FOUND);
            });
            model.addAttribute("task", task);
            model.addAttribute("processorName", taskHelper.getProcessorName(task).orElse(""));
            model.addAttribute("taskHelper", taskHelper);
            return "tasks/taskInterface";
        }

        return "tasks/tasks";
    }

    @GetMapping("/task/info")
    public String taskInfo(Model model, @RequestParam("q") UUID id) throws SQLException {
        TaskMessage message = cache.getTaskMessageService().getMessageById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        model.addAttribute("message", message);
        return "tasks/taskMessage";
    }

    @GetMapping("/task/create/person")
    public RedirectView createTaskForPerson(@RequestParam UUID id) throws SQLException {
        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Task task = new Task(UUID.randomUUID(), 0, "null", "Neuer Vorgang", "Keine Beschreibung",
                Optional.of(person.getId()), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                TaskType.JOB, TaskCategory.NULL, Optional.of(id), TaskState.REDIRECTED, CMSDate.current(),
                CMSDate.current(), CMSDate.current(), Optional.empty(), "", "", TaskPriority.NULL,
                Optional.empty(), new ArrayList<>());

       cache.getTaskService().saveTask(task);
        LOGGER.info("Created redirected task for person {}", id);
        return new RedirectView("/");
    }

    @PostMapping("/task/message/create")
    public RedirectView createTaskMessage(@RequestParam String messageTitle,
                                          @RequestParam String messageDescription,
                                          @RequestParam String messageInformationCascade,
                                          @RequestParam Optional<MultipartFile> file,
                                          @RequestParam UUID id) throws SQLException {

        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        TaskMessage message = new TaskMessage(UUID.randomUUID(), MessageType.COMMENT, messageTitle, messageDescription,
                MessageInformationCascade.valueOf(messageInformationCascade), CMSDate.current(), Optional.of(person.getId()), file.isPresent());

        try {
            if (file.isPresent() && !file.get().isEmpty()) {
                saveFile(file.get(), message.getMessageId().toString());
            }
            cache.getTaskMessageService().saveMessage(id, message);
            LOGGER.info("Saved task message {} to task {}", message.getMessageId(), id);
        } catch (IOException e) {
            LOGGER.error("Failed to save file for message {}: {}", message.getMessageId(), e.getMessage(), e);
            return new RedirectView("/finance?q=error&t=fileUploadFailed");
        }

        return new RedirectView("/tasks?id=" + id);
    }

    private void saveFile(MultipartFile file, String id) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";

        Path dirPath = Paths.get("cms_vx", "taskFile");
        Files.createDirectories(dirPath);

        Path filePath = dirPath.resolve(id + extension);
        Files.write(filePath, file.getBytes());

        LOGGER.info("Saved task file: {}", filePath);
    }
}

