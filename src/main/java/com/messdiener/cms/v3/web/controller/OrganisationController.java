package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.event.OrganisationEventHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.helper.tenant.TenantHelper;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OrganisationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationController.class);
    private final Cache cache;
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;
    private final TenantHelper tenantHelper;
    private final OrganisationEventHelper eventHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("OrganisationController initialized.");
    }

    @GetMapping("/organisation/worship")
    public String worship(Model model, HttpSession session, @RequestParam("q") Optional<String> q,
                          @RequestParam("id") Optional<String> id, @RequestParam("t") Optional<String> optionalLong) throws SQLException {

        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(session);

        if (!personHelper.hasPermission(person, "TEAM")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Zugriff verweigert.");
        }

        OrganisationType type = OrganisationType.WORSHIP;
        UUID tenantId = person.getTenantId();

        model.addAttribute("events", cache.getOrganisationEventService().getNextEvents(tenantId, type));
        model.addAttribute("selectMonths", cache.getOrganisationAnalyticsService().getMonthDates(tenantId));
        model.addAttribute("allEvents", cache.getOrganisationEventService().getEvents(type));
        model.addAttribute("tenants", cache.getTenantService().getTenants());
        model.addAttribute("tenantHelper", tenantHelper);
        model.addAttribute("eventHelper", eventHelper);

        String query = q.orElse("null");
        LOGGER.info("WorshipController: query = {}", query);

        switch (query) {
            case "interface":
                LOGGER.info("Returning worshipInterface");
                if (id.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID fehlt");
                UUID uuid = UUID.fromString(id.get());
                OrganisationEvent target = cache.getOrganisationEventService().getEventById(uuid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                model.addAttribute("targetEvent", target);
                return "organisation/subpages/worshipInterface";

            case "plan":
                LOGGER.info("Returning worshipPlaner");
                CMSDate planDate = optionalLong.map(l -> CMSDate.of(Long.parseLong(l))).orElse(null);
                model.addAttribute("persons", planDate != null ?
                        cache.getPersonService().getPersonsByTenantAndType(tenantId, PersonAttributes.Type.MESSDIENER) : List.of());
                model.addAttribute("events", planDate != null ?
                        cache.getOrganisationEventService().getEvents(tenantId, type, planDate.toLong()) : List.of());
                model.addAttribute("monthName", planDate != null ? planDate.convertTo(DateUtils.DateType.MONTH_NAMES) : "Bitte Monat auswählen");
                model.addAttribute("longD", planDate != null ? planDate.toLong() : "");
                return "organisation/subpages/worshipPlaner";

            case "export":
                LOGGER.info("Returning worshipExport");
                CMSDate exportDate = optionalLong.map(l -> CMSDate.of(Long.parseLong(l))).orElse(null);
                model.addAttribute("events", exportDate != null ?
                        cache.getOrganisationEventService().getNextEvents(tenantId, type, exportDate.toLong()) : List.of());
                model.addAttribute("monthName", exportDate != null ? exportDate.convertTo(DateUtils.DateType.MONTH_NAMES) : "Bitte Monat auswählen");
                model.addAttribute("longD", exportDate != null ? exportDate.toLong() : "");
                return "organisation/subpages/worshipExport";

            default:
                return "organisation/worship";
        }
    }


    @PostMapping("/organisation/save/worship")
    public RedirectView saveWorship(@RequestParam String description, @RequestParam String info,
                                    @RequestParam String startDate, @RequestParam String endDate,
                                    @RequestParam Optional<String> openEnd, @RequestParam Optional<String> idS) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        OrganisationEvent event = new OrganisationEvent(
                idS.map(UUID::fromString).orElse(UUID.randomUUID()),
                user.getTenantId(),
                OrganisationType.WORSHIP,
                CMSDate.convert(startDate, DateUtils.DateType.ENGLISH_DATETIME),
                CMSDate.convert(endDate, DateUtils.DateType.ENGLISH_DATETIME),
                openEnd.isPresent(),
                description, info, ""
        );

        cache.getOrganisationEventService().saveEvent(event);
        return new RedirectView("/organisation/worship");
    }

    @GetMapping("/organisation/delete")
    public RedirectView delete(@RequestParam("id") String id) throws SQLException {
        UUID uuid = UUID.fromString(id);
        cache.getOrganisationEventService().deleteEvent(uuid);
        return new RedirectView("/organisation/worship");
    }

    @PostMapping("/organisation/register-event")
    @ResponseBody
    public ResponseEntity<Map<String, String>> registerEvent(@RequestParam String eventId,
                                                             @RequestParam String personId) throws SQLException {
        UUID eId = UUID.fromString(eventId);
        UUID pId = UUID.fromString(personId);
        Person person = cache.getPersonService().getPersonById(pId).orElseThrow();

        boolean scheduled = cache.getOrganisationMappingService().isRegistered(pId, eId);
        cache.getOrganisationMappingService().setMapState(eId, pId, 1, scheduled ? 0 : 1, 0);

        LOGGER.info("Event registration changed: Person [{}] {} event {}", person.getName(), scheduled ? "left" : "joined", eId);
        return ResponseEntity.ok(Map.of("message", String.valueOf(!scheduled)));
    }

}

