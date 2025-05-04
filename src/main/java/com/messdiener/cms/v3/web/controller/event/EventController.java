package com.messdiener.cms.v3.web.controller.event;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.event.Event;
import com.messdiener.cms.v3.app.helper.event.PlanerHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.article.ArticleService;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.event.EventService;
import com.messdiener.cms.v3.app.services.event.PlannerTaskService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.event.EventState;
import com.messdiener.cms.v3.shared.enums.event.EventType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import com.messdiener.cms.v3.web.request.EventForm;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @GetMapping("/event")
    public String calendar(HttpSession httpSession, Model model, @RequestParam("id")Optional<String> idS, @RequestParam("q")Optional<String> q) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        model.addAttribute("personHelper", personHelper);

        model.addAttribute("types", EventType.values());
        model.addAttribute("events", eventService.getEvents());

        String query = q.orElse("null");
        String idString = idS.orElse("null");

        if(query.equals("null")) return "calendar/eventOverview";

        if(query.equals("info") && !idString.equals("null")){

            Event event = eventService.getEventById(UUID.fromString(idString)).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found"));
            model.addAttribute("event", event);
            model.addAttribute("editors", List.of(user));
            model.addAttribute("progress", 10);
            model.addAttribute("states", EventState.values());
            model.addAttribute("managers", personService.getManagers());
            model.addAttribute("files", documentService.getAllDocumentsByTarget(event.getEventId().toString()));
            model.addAttribute("articles", articleService.getArticlesById(event.getEventId().toString()));
            model.addAttribute("audit", auditService.getLogsByConnectId(event.getEventId()));

            model.addAttribute("tasks", plannerTaskService.getTasks(event.getEventId()));

            return "calendar/eventInterface";
        }

        return "calendar/eventOverview";
    }

    @PostMapping("/event/create")
    public String createEvent(@RequestParam("description") String description, @RequestParam("info") String info,
            @RequestParam("startDate") String startDateE, @RequestParam(value = "endDate") Optional<String> endDateE,
            Model model) throws SQLException {


        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Event event = Event.empty(UUID.randomUUID(), user.getTenantId(), description, EventType.valueOf(info), EventState.PLANNING, CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH), CMSDate.generateOptionalString(endDateE, DateUtils.DateType.ENGLISH));
        eventService.save(event);

        planerHelper.createSubTasks(event.getEventId(),EventType.valueOf(info));

        return "redirect:/event"; // oder eine Template-Ansicht wie "eventList"
    }

    @PostMapping("/event/edit")
    public RedirectView handleFormSubmission(@ModelAttribute EventForm eventForm) throws SQLException {

        Event event = eventService.getEventById(eventForm.getId()).orElseThrow(() -> new IllegalArgumentException("Event not Found"));
        event.setTitle(eventForm.getTitel());
        event.setDescription(eventForm.getBeschreibung());
        event.setType(EventType.valueOf(eventForm.getEventType()));
        event.setState(EventState.valueOf(eventForm.getEventState()));
        event.setStartDate(CMSDate.convert(eventForm.getStartDatum(), DateUtils.DateType.ENGLISH_DATETIME));

        if(eventForm.getEndDatum().isPresent()){
            event.setEndDate(Optional.of(CMSDate.convert(eventForm.getEndDatum().get(), DateUtils.DateType.ENGLISH_DATETIME)));
        }else {
            event.setEndDate(Optional.empty());
        }

        event.setTargetGroup(eventForm.getTargetgroup());

        if(eventForm.getManager().isEmpty()){
            event.setManagerId(Cache.SYSTEM_USER);
        }else {
            event.setManagerId(UUID.fromString(eventForm.getManager()));
        }

        event.setImgUrl(eventForm.getImgUrl());

        eventService.save(event);

        return new  RedirectView("/event?q=info&id=" + eventForm.getId());
    }
}
