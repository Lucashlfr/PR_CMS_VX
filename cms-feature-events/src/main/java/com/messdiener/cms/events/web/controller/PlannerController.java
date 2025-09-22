package com.messdiener.cms.events.web.controller;

import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.domain.documents.DocumentQueryPort;
import com.messdiener.cms.domain.documents.DocumentView;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.events.app.service.EventService;
import com.messdiener.cms.events.app.service.PlannerMappingService;
import com.messdiener.cms.events.app.service.PlannerTaskService;
import com.messdiener.cms.events.domain.entity.Event;
import com.messdiener.cms.events.domain.entity.PlanerTask;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.web.common.security.SecurityHelper;
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
    private final DocumentQueryPort documentQueryPort;

    @GetMapping("/planer/task")
    public String getTasks(Model model,
                           @RequestParam("eventId") UUID eventId,
                           @RequestParam("taskId") UUID taskId) throws SQLException {

        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        PlanerTask task = plannerTaskService.getTaskById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        model.addAttribute("event", event);
        model.addAttribute("task", task);
        model.addAttribute("persons", personService.getActiveMessdienerByTenant(user.getTenant()));

        if (task.getLable().equals("PRINCIPAL")) {
            return "calendar/form/principalForm";
        } else if (task.getLable().equals("LOCATION")) {
            return "calendar/form/locationForm";
        } else if (task.getLable().equals("TEXT")) {
            List<DocumentView> docs = documentQueryPort.getAllDocumentsByTarget(taskId.toString());
            DocumentView document = docs.isEmpty()
                    ? null
                    : docs.get(0);
            if (document == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Document not found");
            }
            model.addAttribute("document", document);
            return "calendar/form/textForm";
        } else if (task.getLable().equals("WEBSITE")) {
            model.addAttribute("article", null); // TODO: ArticleQueryPort + Adapter einführen
            model.addAttribute("states", ArticleState.values());
            return "homepage/interface/articleInterface";
        }
        throw new IllegalArgumentException("Type not found");
    }

    @PostMapping("/planer/principal/save")
    public String saveResponsiblePersons(@RequestParam("id") UUID id,
                                         @RequestParam("taskId") UUID taskId,
                                         @RequestParam(value = "selectedPersons", required = false) List<UUID> selectedPersons)
            throws SQLException {

        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow();

        if (selectedPersons == null) {
            selectedPersons = new ArrayList<>();
            task.setState(CMSState.PROGRESS);
        } else {
            task.setState(CMSState.COMPLETED);
        }
        plannerTaskService.updateTask(event.getEventId(), task);

        plannerMappingService.savePrincipal(id, selectedPersons);

        auditService.createLog(AuditLog.of(
                MessageType.INFO, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER,
                "Die Verantwortlichen Personen wurden von " + user.getName() + " bearbeitet.", ""
        ));

        return "close-popup";
    }

    @PostMapping("/planer/location/save")
    public String saveLocation(@RequestParam("id") UUID id,
                               @RequestParam("taskId") UUID taskId,
                               @RequestParam("location") String location) throws SQLException {

        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.setLocation(location);
        eventService.save(event);

        auditService.createLog(AuditLog.of(
                MessageType.INFO, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER,
                "Der Veranstaltungsort wurde von " + user.getName() + " bearbeitet.", ""
        ));

        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow();
        if (location.isEmpty()) {
            task.setState(CMSState.PROGRESS);
        } else {
            task.setState(CMSState.COMPLETED);
        }
        plannerTaskService.updateTask(event.getEventId(), task);

        return "close-popup";
    }

    @PostMapping("/planer/insurance/risk")
    public String evaluateRisk(@RequestParam("id") UUID id,
                               @RequestParam("taskId") UUID taskId,
                               @RequestParam(name = "risikoOptionen", required = false) List<Integer> risikoOptionen)
            throws SQLException {

        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        PlanerTask task = plannerTaskService.getTaskById(taskId).orElseThrow();
        if (risikoOptionen == null || risikoOptionen.isEmpty()) {
            task.setState(CMSState.PROGRESS);
        } else {
            int counter = Math.max(0, risikoOptionen.stream().mapToInt(Integer::intValue).sum());
            task.setState(CMSState.COMPLETED);
            event.setRiskIndex(counter);
            auditService.createLog(AuditLog.of(
                    MessageType.INFO, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER,
                    "Die Risikoanalyse wurde von " + user.getName() + " durchgeführt.", ""
            ));
        }
        plannerTaskService.updateTask(event.getEventId(), task);
        return "close-popup";
    }

    @GetMapping("/planer/task/check")
    public String checkTask(@RequestParam("eventId") UUID eventId,
                            @RequestParam("taskId") UUID taskId) throws SQLException {

        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        PlanerTask task = plannerTaskService.getTaskById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setState(CMSState.COMPLETED);
        plannerTaskService.updateTask(event.getEventId(), task);

        auditService.createLog(AuditLog.of(
                MessageType.ENDE, ActionCategory.EVENT, event.getEventId(), Cache.SYSTEM_USER,
                "Die Task " + task.getTaskName() + " wurde von " + user.getName() + " durchgeführt.", ""
        ));

        return "close-popup";
    }
}
