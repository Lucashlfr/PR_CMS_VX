package com.messdiener.cms.liturgy.web.controller;

import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.liturgy.app.helper.LiturgieHelper;
import com.messdiener.cms.liturgy.domain.entity.EventParticipationDto;
import com.messdiener.cms.liturgy.domain.entity.Liturgie;
import com.messdiener.cms.liturgy.domain.entity.LiturgieRequest;
import com.messdiener.cms.liturgy.domain.entity.LiturgieView;
import com.messdiener.cms.liturgy.persistence.service.LiturgieMappingService;
import com.messdiener.cms.liturgy.persistence.service.LiturgieRequestService;
import com.messdiener.cms.liturgy.persistence.service.LiturgieService;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.shared.enums.LiturgieType;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.utils.time.DateUtils;
import com.messdiener.cms.web.common.security.SecurityHelper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

@RequiredArgsConstructor
@Controller
public class LiturgieController {

    private final SecurityHelper securityHelper;
    private final LiturgieHelper liturgieHelper;
    private final LiturgieService liturgieService;
    private final PersonService personService;
    private final LiturgieMappingService liturgieMappingService;
    private final LiturgieRequestService liturgieRequestService;

    @GetMapping("/liturgie")
    public String liturgie(HttpSession session, Model model, @RequestParam("startDate") Optional<String> startDateS, @RequestParam("endDate") Optional<String> endDateS, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> idS) throws SQLException {
        PersonSessionView sessionUser = securityHelper.addPersonToSession(session)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person person = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        liturgieHelper.extractedLoadMethod(model, startDateS, endDateS, person, Optional.empty());

        List<EventParticipationDto> participation = liturgieHelper.getEventParticipation(startDateS, endDateS, person, Optional.empty());
        model.addAttribute("eventParticipations", participation);

        model.addAttribute("types", LiturgieType.values());
        model.addAttribute("q", q.orElse("list"));

        if (q.isPresent() && q.get().equals("edit") && idS.isPresent()) {
            Liturgie liturgie = liturgieService.getLiturgie(UUID.fromString(idS.get())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liturgie not found"));
            model.addAttribute("liturgie", liturgie);
            return "liturgie/interface/liturgieInfo";
        } else if (q.isPresent() && q.get().equals("request")) {
            model.addAttribute("current", liturgieRequestService.currentRequest(person.getTenant()).isPresent());

            Optional<LiturgieRequest> request = liturgieRequestService.currentRequest(person.getTenant());
            model.addAttribute("currentRequest", request.orElse(null));

            Map<String, Boolean> status = new HashMap<>();
            if (request.isPresent()) {
                status = liturgieRequestService.getPersonStatusMap(person.getTenant(), request.get().getRequestId());
            }
            model.addAttribute("status", status);


            return "liturgie/list/liturgieRequest";
        }else if (q.isPresent() && q.get().equals("statistic")) {
            model.addAttribute("current", liturgieRequestService.currentRequest(person.getTenant()).isPresent());

            Optional<LiturgieRequest> request = liturgieRequestService.currentRequest(person.getTenant());
            model.addAttribute("currentRequest", request.orElse(null));

            Map<String, Boolean> status = new HashMap<>();
            if (request.isPresent()) {
                status = liturgieRequestService.getPersonStatusMap(person.getTenant(), request.get().getRequestId());
            }
            model.addAttribute("status", status);


            return "liturgie/list/liturgieStatistic";
        }

        return "liturgie/list/liturgieOverview";
    }

    @PostMapping("/liturgie/ajax")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleRegistration(@RequestParam("liturgieId") UUID liturgieId, @RequestParam("personId") UUID personId) {
        LiturgieState current = liturgieMappingService.getState(liturgieId, personId);
        LiturgieState next = (current == LiturgieState.DUTY)
                ? LiturgieState.AVAILABLE
                : LiturgieState.DUTY;

        liturgieMappingService.setState(liturgieId, personId, next);

        boolean isNowRegistered = (next == LiturgieState.DUTY);
        return ResponseEntity
                .ok(Map.of("message", String.valueOf(isNowRegistered)));

    }

    @GetMapping("/liturgie/export")
    public String export(Model model, HttpSession session,
                         @RequestParam(value = "startDate", required = false) String startDateS,
                         @RequestParam(value = "endDate", required = false) String endDateS) throws SQLException {

        securityHelper.addPersonToSession(session);
        PersonSessionView sessionUser = securityHelper.addPersonToSession(session)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person person = personService.getPersonById(sessionUser.id()).orElseThrow();

        startDateS = (startDateS == null || startDateS.isEmpty()) ? CMSDate.of(liturgieHelper.getStartOfCurrentMonthMillis()).getEnglishDate() : startDateS;
        endDateS = (endDateS == null || endDateS.isEmpty()) ? CMSDate.of(liturgieHelper.getEndOfCurrentMonthMillis()).getEnglishDate() : endDateS;

        CMSDate startDate = CMSDate.convert(startDateS, DateUtils.DateType.ENGLISH);
        CMSDate endDate = CMSDate.convert(endDateS, DateUtils.DateType.ENGLISH);


        List<Liturgie> liturgieList = liturgieService.getLiturgies(person.getTenant(), startDate.toLong(), endDate.toLong());
        List<PersonOverviewDTO> persons = personService.getActiveMessdienerByTenantDTO(person.getTenant());
        Map<UUID, Map<UUID, LiturgieState>> stateMap = liturgieMappingService.getStatesForLiturgies(liturgieList, persons);

        List<LiturgieView> views = liturgieList.stream()
                .map(l -> new LiturgieView(
                        l.getLiturgieId(),
                        l.getLiturgieType().getLabel(),
                        l.getDate(),
                        stateMap.get(l.getLiturgieId())))
                .toList();

        model.addAttribute("liturgies", liturgieList);
        model.addAttribute("persons", persons);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("liturgieViews", views);

        return "liturgie/list/liturgieExport";
    }

    @PostMapping("/liturgie/create")
    public RedirectView createLiturgie(@RequestParam("type") LiturgieType type, @RequestParam("date") String dateE, @RequestParam("overall") Optional<String> overall) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person person = personService.getPersonById(sessionUser.id()).orElseThrow();
        Liturgie liturgie = new Liturgie(UUID.randomUUID(), 0, person.getTenant(), type, CMSDate.convert(dateE, DateUtils.DateType.ENGLISH_DATETIME), overall.isEmpty());
        liturgieService.save(liturgie);
        return new RedirectView("/liturgie?state=created");
    }

    @PostMapping("/liturgie/update")
    public RedirectView updateLiturgie(@RequestParam("id") UUID id, @RequestParam("type") LiturgieType type, @RequestParam("date") String dateE, @RequestParam("overall") Optional<String> overall) throws SQLException {
        Liturgie liturgie = liturgieService.getLiturgie(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liturgie not found"));
        liturgie.setLiturgieType(type);
        liturgie.setDate(CMSDate.convert(dateE, DateUtils.DateType.ENGLISH_DATETIME));
        liturgie.setLocal(overall.isEmpty());
        liturgieService.save(liturgie);
        return new RedirectView("/liturgie?q=edit&state=updated&id=" + id);
    }

}
