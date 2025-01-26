package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.*;

@Controller
public class OrganisationController {

    @GetMapping("/organisation/worship")
    public String worship(Model model, HttpSession httpSession, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> id, @RequestParam("t") Optional<String> optionalLong) throws SQLException {
        Person user = SecurityHelper.addPersonToSession(httpSession);

        List<OrganisationEvent> events = Cache.getOrganisationService().getNextEvents(user.getTenantId(), OrganisationType.WORSHIP);
        model.addAttribute("events", events);
        model.addAttribute("selectMonths", Cache.getOrganisationService().getMonthNames(user.getTenant().getId()));

        String query = q.orElse("null");
        switch (query) {

            case "interface":
                id.orElseThrow();
                UUID uuid = UUID.fromString(id.get());
                model.addAttribute("targetEvent", Cache.getOrganisationService().getEventById(uuid).orElseThrow());
                return "organisation/subpages/worshipInterface";

            case "plan":

                if (optionalLong.isPresent()) {

                    CMSDate date = CMSDate.convert(optionalLong.get(), DateUtils.DateType.MONTH_NAMES);

                    model.addAttribute("persons", Cache.getPersonService().getPersonsByTenantAndType(user.getTenantId(), PersonAttributes.Type.MESSDIENER));
                    model.addAttribute("events", Cache.getOrganisationService().getEvents(user.getTenantId(), OrganisationType.WORSHIP, date.toLong()));
                    model.addAttribute("monthName", date.convertTo(DateUtils.DateType.MONTH_NAMES));
                } else {
                    model.addAttribute("persons", new ArrayList<Person>());
                    model.addAttribute("events", new ArrayList<OrganisationEvent>());
                    model.addAttribute("monthName", "Bitte Monat auswählen");
                }


                return "organisation/subpages/worshipPlaner";

            case "export":
                if (optionalLong.isPresent()) {

                    CMSDate date = CMSDate.convert(optionalLong.get(), DateUtils.DateType.MONTH_NAMES);
                    System.out.println(date.convertTo(DateUtils.DateType.MONTH_NAMES));
                    System.out.println(date.getGermanDate());

                    model.addAttribute("events", Cache.getOrganisationService().getNextEvents(user.getTenantId(), OrganisationType.WORSHIP, date.toLong()));
                    model.addAttribute("monthName", date.convertTo(DateUtils.DateType.MONTH_NAMES));
                } else {
                    model.addAttribute("events", new ArrayList<OrganisationEvent>());
                    model.addAttribute("monthName", "Bitte Monat auswählen");
                }
                return "organisation/subpages/worshipExport";

            default:
                return "organisation/worship";
        }
    }

    @PostMapping("/organisation/save/worship")
    public RedirectView saveWorship(@RequestParam("description") String description, @RequestParam("info") String info,
                                    @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate,
                                    @RequestParam("openEnd") Optional<String> openEnd, @RequestParam("id") Optional<String> idS) throws SQLException {

        Person user = SecurityHelper.getPerson();
        OrganisationEvent event = new OrganisationEvent(UUID.randomUUID(), user.getTenantId(), OrganisationType.WORSHIP, CMSDate.convert(startDate, DateUtils.DateType.ENGLISH_DATETIME), CMSDate.convert(endDate, DateUtils.DateType.ENGLISH_DATETIME), openEnd.isPresent(), description, info, "");

        if (idS.isPresent()) {
            UUID id = UUID.fromString(idS.get());
            event.setId(id);
        }

        event.save();

        return new RedirectView("/organisation/worship");

    }

    @GetMapping("/organisation/delete")
    public RedirectView delete(@RequestParam("id") String id) throws SQLException {
        UUID uuid = UUID.fromString(id);
        Cache.getOrganisationService().deleteEvent(uuid);
        return new RedirectView("/organisation/worship");
    }


    @PostMapping("/organisation/register-event")
    @ResponseBody
    public ResponseEntity<Map<String, String>> registerEvent(
            @RequestParam String eventId,
            @RequestParam String personId) throws SQLException {
        Person person = Cache.getPersonService().getPersonById(UUID.fromString(personId)).orElseThrow();
        boolean scheduled = person.isScheduled(UUID.fromString(eventId));

        int i;
        if (scheduled) {
            i = 0;
        } else {
            i = 1;
        }

        Cache.getOrganisationService().setMapState(UUID.fromString(eventId), UUID.fromString(personId), 1, i, 0);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", (!scheduled) + ""));
    }


}
