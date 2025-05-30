package com.messdiener.cms.v3.web.controller.liturgie;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.EventParticipationDto;
import com.messdiener.cms.v3.app.entities.worship.HeatmapDto;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.entities.worship.LiturgieView;
import com.messdiener.cms.v3.app.helper.liturgie.LiturgieHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieMappingService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class LiturgieController {

    private final SecurityHelper securityHelper;
    private final LiturgieHelper liturgieHelper;
    private final LiturgieService liturgieService;
    private final PersonService personService;
    private final LiturgieMappingService liturgieMappingService;

    @GetMapping("/liturgie")
    public String liturgie(HttpSession session, Model model, @RequestParam("startDate")Optional<String> startDateS,  @RequestParam("endDate")Optional<String> endDateS) throws SQLException {
        Person person = securityHelper.addPersonToSession(session).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        liturgieHelper.extractedLoadMethod(model, startDateS, endDateS, person, Optional.empty());

        List<EventParticipationDto> participations = liturgieHelper.getEventParticipation(startDateS, endDateS, person, Optional.empty());
        model.addAttribute("eventParticipations", participations);

        return "liturgie/list/liturgieOverview";
    }

    @PostMapping("/liturgie/ajax")
    @ResponseBody
    public ResponseEntity<Map<String,String>> toggleRegistration(@RequestParam("liturgieId") UUID liturgieId, @RequestParam("personId")  UUID personId) {
        try {
            LiturgieState current = liturgieMappingService.getState(liturgieId, personId);
            LiturgieState next = (current == LiturgieState.DUTY)
                    ? LiturgieState.AVAILABLE
                    : LiturgieState.DUTY;

            liturgieMappingService.setState(liturgieId, personId, next);

            boolean isNowRegistered = (next == LiturgieState.DUTY);
            return ResponseEntity
                    .ok(Map.of("message", String.valueOf(isNowRegistered)));

        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Fehler beim Umschalten der Registrierung", e);
        }
    }

    @GetMapping("/liturgie/export")
    public String export(Model model, HttpSession session,
            @RequestParam(value = "startDate", required = false) String startDateS,
            @RequestParam(value = "endDate", required = false)   String endDateS) throws SQLException {

        securityHelper.addPersonToSession(session);
        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        startDateS = (startDateS == null || startDateS.isEmpty()) ? CMSDate.of(liturgieHelper.getStartOfCurrentMonthMillis()).getEnglishDate() : startDateS;
        endDateS   = (endDateS   == null || endDateS.isEmpty()) ? CMSDate.of(liturgieHelper.getEndOfCurrentMonthMillis()).getEnglishDate() : endDateS;

        CMSDate startDate = CMSDate.convert(startDateS, DateUtils.DateType.ENGLISH);
        CMSDate endDate   = CMSDate.convert(endDateS,   DateUtils.DateType.ENGLISH);


        List<Liturgie> liturgieList =  liturgieService.getLiturgies(person.getTenantId(), startDate.toLong(), endDate.toLong());
        List<PersonOverviewDTO> persons = personService.getActiveMessdienerByTenantDTO(person.getTenantId());
        Map<UUID, Map<UUID, LiturgieState>> stateMap = liturgieMappingService.getStatesForLiturgies(liturgieList, persons);

        List<LiturgieView> views = liturgieList.stream()
                .map(l -> new LiturgieView(
                        l.getLiturgieId(),
                        l.getLiturgieType().getLabel(),
                        l.getDate(),
                        stateMap.get(l.getLiturgieId())))
                .toList();

        model.addAttribute("liturgies",liturgieList);
        model.addAttribute("persons", persons);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("liturgieViews", views);

        return "liturgie/list/liturgieExport";
    }
}
