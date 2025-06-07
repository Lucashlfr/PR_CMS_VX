package com.messdiener.cms.v3.web.controller.liturgie;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.*;
import com.messdiener.cms.v3.app.helper.liturgie.LiturgieHelper;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieMappingService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieRequestService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
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
    public String liturgie(HttpSession session, Model model, @RequestParam("startDate")Optional<String> startDateS,  @RequestParam("endDate")Optional<String> endDateS, @RequestParam("q")Optional<String> q, @RequestParam("id")Optional<String> idS) throws SQLException {
        Person person = securityHelper.addPersonToSession(session).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        liturgieHelper.extractedLoadMethod(model, startDateS, endDateS, person, Optional.empty());

        List<EventParticipationDto> participation = liturgieHelper.getEventParticipation(startDateS, endDateS, person, Optional.empty());
        model.addAttribute("eventParticipations", participation);

        model.addAttribute("types", LiturgieType.values());
        model.addAttribute("q", q.orElse("list"));

        if(q.isPresent() && q.get().equals("edit") && idS.isPresent()) {
            Liturgie liturgie = liturgieService.getLiturgie(UUID.fromString(idS.get())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liturgie not found"));
            model.addAttribute("liturgie", liturgie);
            return "liturgie/interface/liturgieInfo";
        }else if(q.isPresent() && q.get().equals("request")) {
            model.addAttribute("current", liturgieRequestService.currentRequest(person.getTenantId()).isPresent());

            Optional<LiturgieRequest> request = liturgieRequestService.currentRequest(person.getTenantId());
            model.addAttribute("currentRequest", request.orElse(null));

            Map<String, Boolean> status = new HashMap<>();
            if(request.isPresent()) {
                status = liturgieRequestService.getPersonStatusMap(person.getTenantId(), request.get().getRequestId());
            }
            model.addAttribute("status", status);


            return "liturgie/list/liturgieRequest";
        }

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

    @PostMapping("/liturgie/create")
    public RedirectView createLiturgie(@RequestParam("type")LiturgieType type, @RequestParam("date")String dateE, @RequestParam("overall")Optional<String> overall) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Liturgie liturgie = new Liturgie(UUID.randomUUID(), 0, person.getTenantId(), type, CMSDate.convert(dateE, DateUtils.DateType.ENGLISH_DATETIME), overall.isEmpty());
        liturgieService.save(liturgie);
        return new RedirectView("/liturgie?state=created");
    }

    @PostMapping("/liturgie/update")
    public RedirectView updateLiturgie(@RequestParam("id") UUID id, @RequestParam("type")LiturgieType type, @RequestParam("date")String dateE, @RequestParam("overall")Optional<String> overall) throws SQLException {
        Liturgie liturgie = liturgieService.getLiturgie(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liturgie not found"));
        liturgie.setLiturgieType(type);
        liturgie.setDate(CMSDate.convert(dateE, DateUtils.DateType.ENGLISH_DATETIME));
        liturgie.setLocal(overall.isEmpty());
        liturgieService.save(liturgie);
        return new RedirectView("/liturgie?q=edit&state=updated&id=" + id);
    }

}
