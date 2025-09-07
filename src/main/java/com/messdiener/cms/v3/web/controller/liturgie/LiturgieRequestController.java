package com.messdiener.cms.v3.web.controller.liturgie;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.entities.worship.LiturgieRequest;
import com.messdiener.cms.v3.app.helper.liturgie.LiturgieHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieMappingService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieRequestService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.shared.enums.RequestEnum;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class LiturgieRequestController {

    private final SecurityHelper securityHelper;
    private final LiturgieRequestService liturgieRequestService;
    private final PersonHelper personHelper;
    private final LiturgieService liturgieService;
    private final LiturgieMappingService liturgieMappingService;

    @PostMapping("/liturgie/request/create")
    public RedirectView createLiturgie(@RequestParam("name")String name, @RequestParam("startDate")String startDateE, @RequestParam("endDate")String endDateE, @RequestParam("deadline")String deadlineE) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        LiturgieRequest liturgieRequest = new LiturgieRequest(UUID.randomUUID(), person.getTenant(), person.getId(), 0, name, CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH), CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH),CMSDate.convert(deadlineE, DateUtils.DateType.ENGLISH), true);
        liturgieRequestService.saveRequest(liturgieRequest);
        return new RedirectView("/liturgie?q=request");
    }

    @GetMapping("/liturgie/request/stop")
    public RedirectView stopLiturgie(@RequestParam("id")UUID id) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        LiturgieRequest liturgieRequest = liturgieRequestService.currentRequest(person.getTenant()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liturgie not found"));
        liturgieRequest.setActive(false);
        liturgieRequestService.saveRequest(liturgieRequest);
        return new RedirectView("/liturgie?q=request");
    }


    @GetMapping("/liturgie/request")
    public String liturgie(HttpSession session, Model model) throws SQLException {
        Person person = securityHelper.addPersonToSession(session).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(session);

        Optional<LiturgieRequest> liturgieRequest = liturgieRequestService.currentRequest(person.getTenant());
        model.addAttribute("current", liturgieRequest.isPresent());
        model.addAttribute("liturgieRequest", liturgieRequest.orElse(null));
        model.addAttribute("personHelper",  personHelper);

        RequestEnum requestEnum = RequestEnum.NO_REQUEST;

        List<Liturgie> liturgieList = new ArrayList<>();
        if(liturgieRequest.isPresent()) {
            liturgieList = liturgieService.getLiturgies(person.getTenant(), liturgieRequest.get().getStartDate().toLong(), liturgieRequest.get().getEndDate().toLong());
            requestEnum = RequestEnum.SHOW;

            if(liturgieRequestService.sendRequest(person.getId(), liturgieRequest.get().getRequestId())) {
                requestEnum = RequestEnum.COMPLETED;
            }

        }
        model.addAttribute("liturgieList", liturgieList);
        model.addAttribute("request", requestEnum);


        model.addAttribute("connections", personHelper.getConnections(person));
        return "liturgie/interface/liturgieRequestInterface";
    }

    @PostMapping("/liturgie/request/submit")
    public RedirectView submitLiturgieRequest(@RequestParam Map<String, String> params,
                                              HttpServletRequest request, @RequestParam(name = "connectionIds", required = false)
                                                  List<UUID> connectionIds) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<UUID> personIds = new ArrayList<>();
        personIds.add(person.getId());

        if (connectionIds != null) {
            personIds.addAll(connectionIds);
        }

        for(UUID pId : personIds) {
            params.forEach((key, value) -> {
                if (key.startsWith("event_")) {
                    String liturgieId = key.substring("event_".length());
                    boolean isAvailable = "1".equals(value);

                    try {
                        liturgieMappingService.setState(UUID.fromString(liturgieId), pId, isAvailable ? LiturgieState.AVAILABLE : LiturgieState.UNAVAILABLE);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            Optional<LiturgieRequest> liturgieRequest = liturgieRequestService.currentRequest(person.getTenant());
            liturgieRequestService.acceptRequest(pId, liturgieRequest.orElseThrow(() -> new IllegalStateException("LiturgieRequest not found")).getRequestId());
        }


        // Weiterleitung nach dem Speichern
        return new RedirectView("/liturgie/request");
    }


}
