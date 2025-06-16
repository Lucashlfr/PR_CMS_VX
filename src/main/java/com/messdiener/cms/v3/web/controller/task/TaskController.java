package com.messdiener.cms.v3.web.controller.task;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.task.Task;
import com.messdiener.cms.v3.app.services.taskService.TaskService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class TaskController {

    private final SecurityHelper securityHelper;
    private final TaskService taskService;

    @GetMapping("/task/create")
    public RedirectView createTask(@RequestParam("link") UUID id, HttpServletRequest request) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new IllegalArgumentException("Person not Found"));

        Task task = new Task(UUID.randomUUID(),0,id,person.getId(),person.getId(),CMSDate.current(),CMSDate.current(),CMSDate.of(-1),"Neue Aufgabe","",CMSState.REDIRECTED, "P3");
        taskService.saveTask(task);

        // Referrer auslesen
        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }

    @GetMapping("/task/state")
    public RedirectView stateTask(@RequestParam("state") CMSState state, @RequestParam("task")UUID id, HttpServletRequest request) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new IllegalArgumentException("Person not Found"));

        Task task = taskService.getTaskById(id).orElseThrow(() -> new IllegalArgumentException("Task not Found"));
        task.setTaskState(state);
        taskService.saveTask(task);

        // Referrer auslesen
        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }

    @PostMapping("/task/edit")
    public RedirectView editTask(@RequestParam("task")UUID id, @RequestParam("titel")String titel, @RequestParam("description")String description, HttpServletRequest request) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new IllegalArgumentException("Person not Found"));

        Task task = taskService.getTaskById(id).orElseThrow(() -> new IllegalArgumentException("Task not Found"));
        task.setTitle(titel);
        task.setDescription(description);
        taskService.saveTask(task);

        // Referrer auslesen
        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }


}
