package com.messdiener.cms.events.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.domain.documents.StorageFileView;
import com.messdiener.cms.domain.documents.StorageQueryPort;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.events.app.helper.PlanerHelper;
import com.messdiener.cms.events.app.service.EventApplicationService;
import com.messdiener.cms.events.app.service.EventMessageService;
import com.messdiener.cms.events.app.service.EventService;
import com.messdiener.cms.events.app.service.PreventionService;
import com.messdiener.cms.events.domain.entity.Event;
import com.messdiener.cms.events.domain.entity.PreventionForm;
import com.messdiener.cms.events.domain.entity.data.MessageItem;
import com.messdiener.cms.events.web.request.EventForm;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.shared.enums.ComponentType;
import com.messdiener.cms.shared.enums.event.EventMessageType;
import com.messdiener.cms.shared.enums.event.EventState;
import com.messdiener.cms.shared.enums.event.EventType;
import com.messdiener.cms.shared.ui.Component;
import com.messdiener.cms.task.persistence.service.TaskService;
import com.messdiener.cms.utils.other.ImgUtils;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.utils.time.DateUtils;
import com.messdiener.cms.web.common.security.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;
    private final PersonService personService;
    private final AuditService auditService;
    private final PlanerHelper planerHelper;
    private final StorageQueryPort storageQueryPort;
    private final EventApplicationService eventApplicationService;
    private final EventMessageService eventTimelineService;
    private final TaskService taskService;
    private final PreventionService preventionService;
    private final EventMessageService eventMessageService;

    @GetMapping("/event")
    public String calendar(
            HttpSession httpSession,
            Model model,
            @RequestParam("id") Optional<String> idS,
            @RequestParam("q") Optional<String> q,
            @RequestParam("s") Optional<String> s,
            @RequestParam("t") Optional<String> t,
            Map<?, ?> map
    ) throws SQLException, JsonProcessingException {

        final String NULL = "null";

        // --- Security & Session ---
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        securityHelper.addPersonToSession(httpSession);

        // --- Static Model Helper ---
        model.addAttribute("personHelper", personHelper);
        model.addAttribute("types", EventType.values());
        model.addAttribute("events", eventService.getEvents());

        // --- Query-Parameter Normalisierung (fachliche Logik bleibt: "null"-String) ---
        final String query = q.orElse(NULL);
        final String idString = idS.orElse(NULL);

        // Übersicht, wenn kein q
        if (NULL.equals(query)) {
            return "list/eventOverview";
        }

        // --- Detailansicht: q=info + id vorhanden ---
        if ("info".equals(query) && !NULL.equals(idString)) {

            // UUID parsen (gleiches Verhalten bei ungültiger UUID wie zuvor)
            UUID eventId = UUID.fromString(idString);

            Event event = eventService.getEventById(eventId)
                    // BEIBEHALTEN: 401 bei "Event not found"
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found"));

            // Basis-Modeldaten
            model.addAttribute("event", event);
            model.addAttribute("editors", List.of(user));
            model.addAttribute("progress", 10);
            model.addAttribute("states", EventState.values());
            model.addAttribute("managers", personService.getManagers());
            // BEIBEHALTEN: "files" zunächst aus documentQueryPort (wird später ggf. überschrieben)
            model.addAttribute("files", storageQueryPort.getFiles(event.getEventId()));
            model.addAttribute("articles", Collections.emptyList());
            model.addAttribute("audit", auditService.getLogsByConnectId(event.getEventId()));

            // Namen
            model.addAttribute("name1", personHelper.getName(event.getCurrentEditor()));
            model.addAttribute("name2", personHelper.getName(event.getCreatedBy()));
            model.addAttribute("name3", personHelper.getName(event.getPrincipal()));
            model.addAttribute("name4", personHelper.getName(event.getManager()));

            // Tasks & Personen
            model.addAttribute("tasks", taskService.getTasksByLink(event.getEventId()));
            List<PersonOverviewDTO> persons = personService.getActiveMessdienerByTenantDTO(event.getTenant());
            model.addAttribute("persons", persons);

            // Statistiken (unverändert)
            int personSize = persons.size();
            List<String[]> exportRows = eventApplicationService.exportEventResults(event.getEventId());
            int mapping = Math.toIntExact(
                    exportRows.stream().filter(row -> row.length > 2 && row[2] != null && row[2].contains("#")).count()
            );
            int active = exportRows.size() - mapping - 1;
            int noFeedback = personSize - active - mapping;

            model.addAttribute("mapping", mapping);
            model.addAttribute("noFeedback", noFeedback);
            model.addAttribute("active", active);

            // Sektion s: Attribut setzen wie bisher, aber weiterhin Pflicht
            model.addAttribute("s", s.orElse("info"));
            if (s.isEmpty()) {
                // BEIBEHALTEN: 404 wenn s fehlt
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
            }

            // Files aus Storage (werden je nach View genutzt / überschreiben "files")
            List<StorageFileView> storageFiles = storageQueryPort.getFiles(event.getEventId());

            // Routing je nach s
            if (s.get().equals("tasks")) {
                if (t.isPresent()) {
                    UUID taskId = UUID.fromString(t.get());
                    model.addAttribute("task", taskService.getTaskById(taskId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Task not found")));
                    return "interface/eventInterfaceTaskInterface";
                } else {
                    return "interface/eventInterfaceTask";
                }
            }

            model.addAttribute("eventMessageTypes", EventMessageType.values());
            model.addAttribute("messages", eventMessageService.getItems(event.getEventId()));

            model.addAttribute("components", eventApplicationService.getComponents(event.getEventId()));
            model.addAttribute("cTypes", ComponentType.values());
            model.addAttribute("exportRows", exportRows);

            model.addAttribute("files", storageFiles); // BEIBEHALTEN: Überschreibt "files"
            model.addAttribute("imgUtils", new ImgUtils());
            model.addAttribute("timelineItems", eventTimelineService.getItems(event.getEventId()));
            model.addAttribute("preventionForm", preventionService.getPreventionForm(event.getEventId()));

            return "interface/eventInterfaceInfo";
        }

        // Fallback: Übersicht
        return "list/eventOverview";
    }


    @PostMapping("/event/prevention/submit")
    public RedirectView submitForm(@ModelAttribute PreventionForm form, Model model, HttpServletRequest request) {
        // Validierung / Weiterleitung
        preventionService.savePreventionForm(form);

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }

    @PostMapping("/event/create")
    public String createEvent(@RequestParam("titel") String titel, @RequestParam("eventType") String eventType,
                              @RequestParam("startDate") String startDateE, @RequestParam("endDate") String endDateE,
                              @RequestParam("deadline") String deadlineE, Model model) throws SQLException {


        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Event event = Event.empty(UUID.randomUUID(), user.getTenant(), titel, EventType.valueOf(eventType), EventState.PLANNING, CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH_DATETIME), CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH_DATETIME), CMSDate.convert(deadlineE, DateUtils.DateType.ENGLISH));
        eventService.save(event);

        planerHelper.createSubTasks(event.getEventId(), EventType.valueOf(eventType));
        if (event.getType() == EventType.SMALL_ACTION || event.getType() == EventType.BIG_ACTION) {
            planerHelper.createComponents(event.getEventId());
        }

        return "redirect:/event"; // oder eine Template-Ansicht wie "eventList"
    }

    @PostMapping("/event/edit")
    public RedirectView handleFormSubmission(@ModelAttribute EventForm eventForm) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        Event event = eventService.getEventById(eventForm.getId()).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setTitle(eventForm.getTitel());
        event.setDescription(eventForm.getBeschreibung());
        event.setType(EventType.valueOf(eventForm.getEventType()));
        event.setTargetGroup(eventForm.getTargetgroup());
        event.setState(EventState.valueOf(eventForm.getEventState()));
        event.setStartDate(CMSDate.convert(eventForm.getStartDate(), DateUtils.DateType.ENGLISH_DATETIME));
        event.setEndDate(CMSDate.convert(eventForm.getEndDate(), DateUtils.DateType.ENGLISH_DATETIME));
        event.setDeadline(CMSDate.convert(eventForm.getDeadline(), DateUtils.DateType.ENGLISH));
        event.setCurrentEditor(user.getId());
        event.setLastUpdate(CMSDate.current());
        event.setForms(eventForm.getForms());
        eventService.save(event);

        return new RedirectView("/event?q=info&s=info&id=" + eventForm.getId());
    }

    @PostMapping("/event/edit/application")
    public RedirectView editApplication(@RequestParam("id") UUID id, @RequestParam("html") String html) {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setApplication(html);
        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=info");
    }

    @PostMapping("/event/edit/notes")
    public RedirectView editNotes(@RequestParam("id") UUID id, @RequestParam("content") String comment) {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setNotes(comment);
        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=info");
    }

    @GetMapping("/event/img")
    public RedirectView img(@RequestParam("event") UUID eventId, @RequestParam("img") UUID img) throws SQLException {

        Event event = eventService.getEventById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setImgUrl("/file?id=" + img);
        eventService.save(event);

        return new RedirectView("/event?q=info&s=info&id=" + eventId);
    }

    @PostMapping("/event/component")
    public RedirectView component(@RequestParam("id") UUID id, @RequestParam("number") int number,
                                  @RequestParam("name") String name, @RequestParam("label") String label,
                                  @RequestParam("cType") ComponentType cType) throws SQLException {

        eventApplicationService.saveComponent(id, Component.of(number, cType, name, label, "", "", true));
        return new RedirectView("/event?q=info&s=info&id=" + id);
    }

    @PostMapping("/event/message")
    public RedirectView createMessage(@RequestParam Map<String, String> params) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        UUID eventId = UUID.fromString(params.get("eventId"));
        String title = params.get("title");
        String description = params.get("description");
        EventMessageType type = EventMessageType.valueOf(params.get("eventMessageType"));

        eventMessageService.createMessage(eventId, new MessageItem(UUID.randomUUID(),0, title, description, CMSDate.current(), type, user.getId()));

        return new RedirectView("/event?q=info&s=info&id=" + params.get("eventId"));
    }

    @PostMapping("/event/edit/press")
    public RedirectView editPress(@RequestParam("id") UUID id, @RequestParam("html") String html) throws SQLException {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setPressRelease(html);
        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=info");
    }

    @PostMapping("/event/user/map")
    public RedirectView mapUser(@RequestParam("id") UUID id, @RequestParam("resultId") UUID resultId, @RequestParam("personId") UUID personId) throws SQLException {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        eventApplicationService.updateUserIdForResult(resultId, personId.toString());
        return new RedirectView("/event?q=info&s=info&id=" + id);
    }



}
