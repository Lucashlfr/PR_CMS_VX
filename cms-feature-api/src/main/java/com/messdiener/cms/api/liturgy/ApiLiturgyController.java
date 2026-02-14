package com.messdiener.cms.api.liturgy;

import com.messdiener.cms.api.common.response.ApiResponse;
import com.messdiener.cms.api.common.response.ApiResponseHelper;
import com.messdiener.cms.domain.liturgy.*;
import com.messdiener.cms.domain.person.PersonOverviewLite;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.web.common.security.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/liturgy")
@RequiredArgsConstructor
public class ApiLiturgyController {

    private final LiturgyQueryPort liturgyQueryPort;
    private final LiturgyCommandPort liturgyCommandPort;
    private final SecurityHelper securityHelper;

    // -----------------------------------------------------------
    // NEXT – nächste Termine, zu denen der User DUTY ist
    // -----------------------------------------------------------
    @GetMapping(value = "/next", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> next(HttpServletRequest request) {
        final PersonSessionView user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        long now = System.currentTimeMillis();
        long horizon = now + 90L * 24 * 60 * 60 * 1000; // 90 Tage Sicht – fachlich wie "kommend"

        // 1) Alle kommenden Liturgien im Tenant
        List<LiturgyView> liturgies = liturgyQueryPort.getLiturgies(user.tenant(), now, horizon);

        if (liturgies.isEmpty()) {
            return ApiResponseHelper.ok(List.of(), request, request.getLocale());
        }

        // 2) States nur für den aktuellen User bestimmen
        PersonOverviewLite me = new PersonOverviewLite(user.id(), user.firstName(), user.lastName(), user.tenant());
        Map<UUID, Map<UUID, LiturgieState>> states = liturgyQueryPort.getStatesForLiturgies(liturgies, List.of(me));

        // 3) Filtern auf DUTY und mappen
        List<Map<String, String>> data = liturgies.stream()
                .filter(l -> LiturgieState.DUTY.equals(
                        Optional.ofNullable(states.getOrDefault(l.id(), Map.of()).get(user.id()))
                                .orElse(LiturgieState.UNAVAILABLE)
                ))
                .sorted(Comparator.comparing(v -> Optional.ofNullable(v.date()).map(CMSDate::toLong).orElse(Long.MAX_VALUE)))
                .map(v -> toItem(v, LiturgieState.DUTY))
                .toList();

        return ApiResponseHelper.ok(data, request, request.getLocale());
    }

    // -----------------------------------------------------------
    // ALL – alle kommenden Liturgien (mit persönlichem State)
    // -----------------------------------------------------------
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> all(HttpServletRequest request) {
        final PersonSessionView user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        long now = System.currentTimeMillis();
        long horizon = now + 365L * 24 * 60 * 60 * 1000; // 1 Jahr Sicht

        List<LiturgyView> liturgies = liturgyQueryPort.getLiturgies(user.tenant(), now, horizon);

        if (liturgies.isEmpty()) {
            return ApiResponseHelper.ok(List.of(), request, request.getLocale());
        }

        PersonOverviewLite me = new PersonOverviewLite(user.id(), user.firstName(), user.lastName(), user.tenant());
        Map<UUID, Map<UUID, LiturgieState>> states = liturgyQueryPort.getStatesForLiturgies(liturgies, List.of(me));

        List<Map<String, String>> data = liturgies.stream()
                .map(v -> {
                    LiturgieState s = Optional.ofNullable(states.getOrDefault(v.id(), Map.of()).get(user.id()))
                            .orElse(LiturgieState.UNAVAILABLE);
                    return toItem(v, s);
                })
                .toList();

        return ApiResponseHelper.ok(data, request, request.getLocale());
    }

    // -----------------------------------------------------------
    // DETAIL „Übersicht“
    // -----------------------------------------------------------
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview(
            HttpServletRequest request,
            @PathVariable("id") UUID id
    ) {
        LiturgyView v = liturgyQueryPort.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        Map<String, Object> res = new LinkedHashMap<>(6);
        res.put("id", v.id());
        res.put("title", Optional.ofNullable(v.typeLabel()).orElse(""));
        res.put("date", Optional.ofNullable(v.date()).map(CMSDate::getEnglishDate).orElse(""));
        res.put("time", Optional.ofNullable(v.date()).map(CMSDate::getTime).orElse(""));
        res.put("location", Optional.ofNullable(v.local()).map(loc -> loc ? "Local" : "").orElse("")); // kein Tenant-Name in View, daher neutral
        res.put("description", ""); // Domain-View enthält keine Nummer – placeholder leer

        return ApiResponseHelper.ok(res, request, request.getLocale());
    }

    // -----------------------------------------------------------
    // TAB „Verfügbarkeit“
    // -----------------------------------------------------------
    @GetMapping(value = "/{id}/availability", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailability(
            HttpServletRequest request,
            @PathVariable("id") UUID id
    ) {
        final PersonSessionView user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<PersonStateView> list = liturgyQueryPort.getPersonStatesForLiturgy(user.tenant(), id);

        List<Map<String, Object>> res = list.stream()
                .map(p -> {
                    Map<String, Object> row = new LinkedHashMap<>(3);
                    row.put("personId", p.personId());
                    row.put("name", (Optional.ofNullable(p.firstName()).orElse("") + " " +
                            Optional.ofNullable(p.lastName()).orElse("")).trim());
                    row.put("state", Optional.ofNullable(p.state()).map(Enum::name).orElse("UNAVAILABLE"));
                    return row;
                })
                .sorted(Comparator.comparingInt(row -> {
                    String state = (String) row.get("state");
                    return switch (state) {
                        case "DUTY" -> 0;
                        case "AVAILABLE" -> 1;
                        case "UNAVAILABLE" -> 2;
                        default -> 3;
                    };
                }))
                .toList();


        return ApiResponseHelper.ok(res, request, request.getLocale());
    }

    // -----------------------------------------------------------
    // Action: Unassign
    // -----------------------------------------------------------
    @PostMapping(value = "/{id}/unassign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> unassign(
            HttpServletRequest request,
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> payload
    ) {
        final PersonSessionView user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        final String reason = Optional.ofNullable(payload.get("reason"))
                .map(Object::toString)
                .map(String::trim)
                .orElse("");

        if (reason.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason is required");
        }

        liturgyCommandPort.unassignWithReason(user.id(), id, reason);

        Map<String, String> res = new LinkedHashMap<>(1);
        res.put("status", "ok");
        return ApiResponseHelper.ok(res, request, request.getLocale());
    }

    // -----------------------------------------------------------
    // Mapper
    // -----------------------------------------------------------
    private static Map<String, String> toItem(LiturgyView v, LiturgieState state) {
        Map<String, String> map = new LinkedHashMap<>(7);
        map.put("id", Optional.ofNullable(v.id()).map(UUID::toString).orElse(""));
        map.put("title", Optional.ofNullable(v.typeLabel()).orElse(""));
        map.put("date", Optional.ofNullable(v.date()).map(CMSDate::getGermanDate).orElse(""));
        map.put("time", Optional.ofNullable(v.date()).map(CMSDate::getTime).orElse(""));
        map.put("location", ""); // LiturgyView enthält keinen Tenantnamen – neutral lassen
        map.put("state", state != null ? state.name() : "UNAVAILABLE");
        return map;
    }
}
