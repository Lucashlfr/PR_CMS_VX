package com.messdiener.cms.v3.app.helper.liturgie;


import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.dto.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.EventParticipationDto;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.entities.worship.LiturgieView;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieMappingService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LiturgieHelper {

    private final LiturgieService liturgieService;
    private final PersonService personService;
    private final LiturgieMappingService liturgieMappingService;

    public long getStartOfCurrentMonthMillis() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        ZonedDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone);
        return startOfMonth.toInstant().toEpochMilli();
    }

    public long getEndOfCurrentMonthMillis() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        ZonedDateTime endOfMonth = lastDay.atTime(23, 59).atZone(zone);
        return endOfMonth.toInstant().toEpochMilli();
    }

    public void extractedLoadMethod(Model model, Optional<String> startDateS, Optional<String> endDateS, Person person, Optional<Person> target) throws SQLException {

        model.addAttribute("now", CMSDate.current());

        String startDateE = startDateS.orElse("");
        String endDateE = endDateS.orElse("");

        CMSDate startDate = startDateS.isPresent() ? CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH) : CMSDate.of(getStartOfCurrentMonthMillis());
        CMSDate endDate = endDateS.isPresent() ? CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH) : CMSDate.of(getEndOfCurrentMonthMillis());

        List<Liturgie> liturgieList =  liturgieService.getLiturgies(person.getTenant(), startDate.toLong(), endDate.toLong());

        List<PersonOverviewDTO> persons;
        if(target.isEmpty()){
            persons = personService.getActiveMessdienerByTenantDTO(person.getTenant());
        }else {
            Person targetPerson = target.get();
            persons = List.of(personService.getPersonDTO(targetPerson).orElseThrow());
        }

        model.addAttribute("liturgies",liturgieList);
        model.addAttribute("persons", persons);
        model.addAttribute("startDate", startDateE);
        model.addAttribute("endDate", endDateE);

        Map<UUID, Map<UUID, LiturgieState>> stateMap = liturgieMappingService.getStatesForLiturgies(liturgieList, persons);

        List<LiturgieView> views = liturgieList.stream()
                .map(l -> new LiturgieView(
                        l.getLiturgieId(),
                        l.getLiturgieType().getLabel() + (!l.isLocal() ? " (Pfr.)" : ""),
                        l.getDate(),
                        stateMap.get(l.getLiturgieId())))
                .toList();
        model.addAttribute("liturgieViews", views);
    }

    public List<EventParticipationDto> getEventParticipation(Optional<String> startDateS, Optional<String> endDateS, Person person, Optional<Person> target) throws SQLException {

        String startDateE = startDateS.orElse("");
        String endDateE = endDateS.orElse("");

        CMSDate startDate = startDateS.isPresent() ? CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH) : CMSDate.of(getStartOfCurrentMonthMillis());
        CMSDate endDate = endDateS.isPresent() ? CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH) : CMSDate.of(getEndOfCurrentMonthMillis());

        // 1) Events abrufen
        List<Liturgie> liturgieList = liturgieService.getLiturgies(person.getTenant(), startDate.toLong(), endDate.toLong());

        List<PersonOverviewDTO> persons;
        if(target.isEmpty()){
            persons = personService.getActiveMessdienerByTenantDTO(person.getTenant());
        }else {
            Person targetPerson = target.get();
            persons = List.of(personService.getPersonDTO(targetPerson).orElseThrow());
        }

        // 3) States pro Liturgie und Person holen
        Map<UUID, Map<UUID, LiturgieState>> stateMap = liturgieMappingService.getStatesForLiturgies(liturgieList, persons);

        // 4) DTOs aufbauen
        return liturgieList.stream()
                .map(l -> {
                    Map<UUID, LiturgieState> ps = stateMap.getOrDefault(l.getLiturgieId(), Collections.emptyMap());

                    long dutyCount      = ps.values().stream()
                            .filter(s -> s == LiturgieState.DUTY)
                            .count();
                    long availableCount = ps.values().stream()
                            .filter(s -> s == LiturgieState.AVAILABLE)
                            .count();
                    long unavailableCount = ps.values().stream()
                            .filter(s -> s == LiturgieState.UNAVAILABLE)
                            .count();
                    return new EventParticipationDto(
                            l.getLiturgieId(),
                            l.getDate().getGermanShortDate(),
                            dutyCount,
                            availableCount,
                            unavailableCount
                    );
                })
                .toList();
    }

    public String getDutyPersons(Liturgie l) throws SQLException {
        return liturgieService.getDutyPersons(l);
    }


}
