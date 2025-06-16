package com.messdiener.cms.v3.web.controller.event;

import com.messdiener.cms.v3.app.entities.document.Document;
import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.entities.event.Event;
import com.messdiener.cms.v3.app.entities.event.PlanerTask;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.article.ArticleService;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.event.EventService;
import com.messdiener.cms.v3.app.services.event.PlannerMappingService;
import com.messdiener.cms.v3.app.services.event.PlannerTaskService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.ArticleState;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PlannerController {

    private final SecurityHelper securityHelper;
    private final EventService eventService;
    private final PlannerTaskService plannerTaskService;
    private final PlannerMappingService plannerMappingService;
    private final AuditService auditService;
    private final PersonService personService;
    private final DocumentService documentService;
    private final ArticleService articleService;
    private final PersonHelper personHelper;

    @GetMapping("/planer/task")
    public String getTasks(Model model, @RequestParam("eventId")UUID eventId, @RequestParam("taskId")UUID taskId) throws SQLException {
        Person user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Event event = eventService.getEventById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        model.addAttribute("event", event);
        model.addAttribute("task", task);
        model.addAttribute("persons", personService.getActiveMessdienerByTenant(user.getTenantId()));

        if(task.getLable().equals("PRINCIPAL")){
            return "calendar/form/principalForm";
        }else if(task.getLable().equals("LOCATION")){
            return "calendar/form/locationForm";
        }else if(task.getLable().equals("TEXT")){
            Document document = documentService.getDocument(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Document not found"));
            model.addAttribute("document", document);
            return "calendar/form/textForm";
        }else if(task.getLable().equals("WEBSITE")){
            model.addAttribute("article", articleService.getArticleById(taskId).orElseThrow());
            model.addAttribute("states", ArticleState.values());
            return "homepage/interface/articleInterface";
        }
        throw new IllegalArgumentException("Type not found");
    }

    @PostMapping("/planer/principal/save")
    public String saveResponsiblePersons(@RequestParam("id") UUID id, @RequestParam("taskId") UUID taskId,
                                               @RequestParam(value = "selectedPersons", required = false) List<UUID> selectedPersons) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Event event = eventService.getEventById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));


        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow();
        if (selectedPersons == null) {
            selectedPersons = new ArrayList<>();
            task.setState(CMSState.PROGRESS);
        } else {
            task.setState(CMSState.COMPLETED);
        }
        plannerTaskService.updateTask(event.getEventId(), task);

        plannerMappingService.savePrincipal(id, selectedPersons);

        auditService.createLog(AuditLog.of(MessageType.INFO, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER, "Die Verantwortlichen Personen wurden von " + user.getName() + " bearbeitet.", ""));

        // Weiterleitung oder Neuladen der Seite
        return "close-popup";
    }

    @PostMapping("/planer/location/save")
    public String saveLocation(@RequestParam("id") UUID id, @RequestParam("taskId") UUID taskId, @RequestParam("location") String location) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Event event = eventService.getEventById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.setLocation(location);
        eventService.save(event);

        auditService.createLog(AuditLog.of(MessageType.INFO, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER, "Der Veranstaltungsort wurde von " + user.getName() + " bearbeitet.", ""));


        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow();
        if (location.isEmpty()) {
            task.setState(CMSState.PROGRESS);
        } else {
            task.setState(CMSState.COMPLETED);
        }
        plannerTaskService.updateTask(event.getEventId(), task);

        // Weiterleitung oder Neuladen der Seite
        return "close-popup";
    }

    @PostMapping("/planer/insurance/risk")
    public String evaluateRisk(@RequestParam("id") UUID id, @RequestParam("taskId") UUID taskId,
                                     @RequestParam(name = "risikoOptionen", required = false) List<Integer> risikoOptionen) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Event event = eventService.getEventById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));


        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow();
        if (risikoOptionen == null || risikoOptionen.isEmpty()) {
            task.setState(CMSState.PROGRESS);
        } else {
            int counter = Math.max(0, risikoOptionen.stream()
                    .mapToInt(Integer::intValue)
                    .sum());
            task.setState(CMSState.COMPLETED);
            event.setRiskIndex(counter);
            auditService.createLog(AuditLog.of(MessageType.INFO, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER, "Die Risikoanalyse wurde von " + user.getName() + " durchgeführt.", ""));
        }
        plannerTaskService.updateTask(event.getEventId(), task);
        return "close-popup";
    }

    @GetMapping("/planer/task/check")
    public String checkTask(@RequestParam("eventId") UUID eventId, @RequestParam("taskId")UUID taskId) throws SQLException {
        Person user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Event event = eventService.getEventById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setState(CMSState.COMPLETED);
        plannerTaskService.updateTask(event.getEventId(), task);
        auditService.createLog(AuditLog.of(MessageType.ENDE, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER, "Die Task " + task.getTaskName() + " wurde von " + user.getName() + " durchgeführt.", ""));

        return "close-popup";
    }



}
