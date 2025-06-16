package com.messdiener.cms.v3.web.controller.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.event.PreventionForm;
import com.messdiener.cms.v3.app.entities.event.data.TimelineItem;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.event.Event;
import com.messdiener.cms.v3.app.entities.task.Task;
import com.messdiener.cms.v3.app.helper.event.PlanerHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.article.ArticleService;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.event.*;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.taskService.TaskService;
import com.messdiener.cms.v3.app.utils.ImgUtils;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.ComponentType;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.shared.enums.event.EventState;
import com.messdiener.cms.v3.shared.enums.event.EventType;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import com.messdiener.cms.v3.web.request.EventForm;
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
    private final DocumentService documentService;
    private final AuditService auditService;
    private final PlanerHelper planerHelper;
    private final PlannerTaskService plannerTaskService;
    private final ArticleService articleService;
    private final StorageService storageService;
    private final EventApplicationService eventApplicationService;
    private final EventTimelineService eventTimelineService;
    private final TaskService taskService;
    private final PreventionService preventionService;

    @GetMapping("/event")
    public String calendar(HttpSession httpSession, Model model, @RequestParam("id") Optional<String> idS, @RequestParam("q") Optional<String> q, @RequestParam("s") Optional<String> s, @RequestParam("t") Optional<String> t) throws SQLException, JsonProcessingException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("personHelper", personHelper);

        model.addAttribute("types", EventType.values());
        model.addAttribute("events", eventService.getEvents());

        String query = q.orElse("null");
        String idString = idS.orElse("null");

        if (query.equals("null")) return "calendar/list/eventOverview";

        if (query.equals("info") && !idString.equals("null")) {

            Event event = eventService.getEventById(UUID.fromString(idString)).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found"));
            model.addAttribute("event", event);
            model.addAttribute("editors", List.of(user));
            model.addAttribute("progress", 10);
            model.addAttribute("states", EventState.values());
            model.addAttribute("managers", personService.getManagers());
            model.addAttribute("files", documentService.getAllDocumentsByTarget(event.getEventId().toString()));
            model.addAttribute("articles", articleService.getArticlesById(event.getEventId().toString()));
            model.addAttribute("audit", auditService.getLogsByConnectId(event.getEventId()));

            model.addAttribute("name1", personHelper.getName(event.getCurrentEditor()));
            model.addAttribute("name2", personHelper.getName(event.getCreatedBy()));
            model.addAttribute("name3", personHelper.getName(event.getPrincipal()));
            model.addAttribute("name4", personHelper.getName(event.getManager()));

            model.addAttribute("s", s.orElse("info"));
            if (s.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");


            List<StorageFile> files = storageService.getFiles(event.getEventId(), FileType.EVENT);
            switch (s.get()) {
                case "info":
                    return "calendar/interface/eventInterfaceInfo";
                case "timeline":
                    model.addAttribute("timelineItems", eventTimelineService.getItems(event.getEventId()));
                    return "calendar/interface/eventInterfaceTimeline";
                case "tasks":
                    if (t.isPresent()) {
                        model.addAttribute("task", taskService.getTaskById(UUID.fromString(t.get())).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Task not found")));
                        return "calendar/interface/eventInterfaceTaskInterface";
                    } else {
                        model.addAttribute("tasks", taskService.getTasksByLink(event.getEventId()));
                        return "calendar/interface/eventInterfaceTask";
                    }
                case "files":
                    model.addAttribute("files", files);
                    return "calendar/interface/eventInterfaceFiles";
                case "settings":
                    model.addAttribute("files", files);
                    model.addAttribute("imgUtils", new ImgUtils());
                    return "calendar/interface/eventInterfaceSettings";
                case "application":
                    model.addAttribute("components", eventApplicationService.getComponents(event.getEventId()));
                    model.addAttribute("cTypes", ComponentType.values());
                    model.addAttribute("exportRows", eventApplicationService.exportEventResults(event.getEventId()));
                    return "calendar/interface/eventInterfaceApplication";
                case "pressrelease":
                    return "calendar/interface/eventInterfacePress";
                case "risc":
                    model.addAttribute("preventionForm",preventionService.getPreventionForm(event.getEventId()));
                    return "calendar/interface/eventInterfaceRisk";
            }
        }

        return "calendar/list/eventOverview";
    }

    @PostMapping("/event/prevention/submit")
    public RedirectView submitForm(@ModelAttribute PreventionForm form, Model model,  HttpServletRequest request) throws SQLException {
        // Validierung / Weiterleitung
        preventionService.savePreventionForm(form);

        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }

    @PostMapping("/event/create")
    public String createEvent(@RequestParam("titel") String titel, @RequestParam("eventType") String eventType,
                              @RequestParam("startDate") String startDateE, @RequestParam("endDate") String endDateE,
                              @RequestParam("deadline") String deadlineE, Model model) throws SQLException {


        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Event event = Event.empty(UUID.randomUUID(), user.getTenantId(), titel, EventType.valueOf(eventType), EventState.PLANNING, CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH_DATETIME), CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH_DATETIME), CMSDate.convert(deadlineE, DateUtils.DateType.ENGLISH));
        eventService.save(event);

        planerHelper.createSubTasks(event.getEventId(), EventType.valueOf(eventType));
        if (event.getType() == EventType.SMALL_ACTION || event.getType() == EventType.BIG_ACTION) {
            planerHelper.createComponents(event.getEventId());
        }

        return "redirect:/event"; // oder eine Template-Ansicht wie "eventList"
    }

    @PostMapping("/event/edit")
    public RedirectView handleFormSubmission(@ModelAttribute EventForm eventForm) throws SQLException {

        Event event = eventService.getEventById(eventForm.getId()).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setTitle(eventForm.getTitel());
        event.setDescription(eventForm.getBeschreibung());
        event.setType(EventType.valueOf(eventForm.getEventType()));
        event.setTargetGroup(eventForm.getTargetgroup());
        event.setState(EventState.valueOf(eventForm.getEventState()));
        eventService.save(event);

        return new RedirectView("/event?q=info&s=info&id=" + eventForm.getId());
    }

    @PostMapping("/event/edit/timeline")
    public RedirectView editTimeLine(@RequestParam("id") UUID id, @RequestParam("startDate") String startDateE, @RequestParam("endDate") String endDateE,
                                     @RequestParam("deadline") String deadlineS) throws SQLException {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setStartDate(CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH_DATETIME));
        event.setEndDate(CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH_DATETIME));
        event.setDeadline(CMSDate.convert(deadlineS, DateUtils.DateType.ENGLISH));

        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=timeline");
    }

    @PostMapping("/event/edit/application")
    public RedirectView editApplication(@RequestParam("id") UUID id, @RequestParam("html") String html) throws SQLException {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setApplication(html);
        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=application");
    }

    @PostMapping("/event/edit/notes")
    public RedirectView editNotes(@RequestParam("id") UUID id, @RequestParam("content") String comment) throws SQLException {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setNotes(comment);
        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=notes");
    }

    @GetMapping("/event/img")
    public RedirectView img(@RequestParam("event") UUID eventId, @RequestParam("img") UUID img) throws SQLException {

        Event event = eventService.getEventById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setImgUrl("/file?id=" + img);
        eventService.save(event);

        return new RedirectView("/event?q=info&s=settings&id=" + eventId);
    }

    @PostMapping("/event/component")
    public RedirectView component(@RequestParam("id") UUID id, @RequestParam("number") int number,
                                  @RequestParam("name") String name, @RequestParam("label") String label,
                                  @RequestParam("cType") ComponentType cType) throws SQLException {

        eventApplicationService.saveComponent(id, Component.of(number, cType, name, label, "", "", true));
        return new RedirectView("/event?q=info&s=application&id=" + id);
    }

    @GetMapping("/event/state")
    public RedirectView state(@RequestParam("event") UUID id, @RequestParam("state") EventState state) throws SQLException {
        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setState(state);
        eventService.save(event);

        return new RedirectView("/event?q=info&s=settings&id=" + id);
    }

    @PostMapping("/event/timeline")
    public RedirectView createOrUpdateTimelineItem(@RequestParam Map<String, String> params) throws SQLException {
        String idParam = params.get("id");
        UUID eventId = UUID.fromString(params.get("eventId"));
        String title = params.get("title");
        String description = params.get("description");
        String dateString = params.get("date");

        UUID id = (idParam != null && !idParam.isEmpty()) ? UUID.fromString(idParam) : UUID.randomUUID();

        // Datum parsen
        CMSDate cmsDate = CMSDate.convert(dateString, DateUtils.DateType.ENGLISH);

        eventTimelineService.createTimeline(eventId, new TimelineItem(id, title, description, cmsDate));

        return new RedirectView("/event?q=info&s=timeline&id=" + params.get("eventId"));
    }

    @PostMapping("/event/edit/press")
    public RedirectView editPress(@RequestParam("id") UUID id, @RequestParam("html") String html) throws SQLException {

        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setPressRelease(html);
        eventService.save(event);

        return new RedirectView("/event?q=info&id=" + id + "&s=pressrelease");
    }

}
