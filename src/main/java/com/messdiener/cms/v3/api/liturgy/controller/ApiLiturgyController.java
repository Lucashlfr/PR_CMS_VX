// src/main/java/com/messdiener/cms/v3/api/liturgy/controller/ApiLiturgyController.java
package com.messdiener.cms.v3.api.liturgy.controller;

import com.messdiener.cms.v3.api.common.response.ApiResponseHelper;
import com.messdiener.cms.v3.api.common.response.dto.ApiResponse;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.dto.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieMappingService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.utils.other.Pair;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/liturgy")
@RequiredArgsConstructor
public class ApiLiturgyController {

    private final ApiResponseHelper apiResponseHelper; // bleibt injiziert (nicht entfernt)
    private final LiturgieService liturgieService;
    private final LiturgieMappingService liturgieMappingService;
    private final SecurityHelper securityHelper;

    // -----------------------------------------------------------
    // NEXT
    // -----------------------------------------------------------
    @GetMapping(value = "/next", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> next(HttpServletRequest request) {
        final Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        final List<Pair<Liturgie, LiturgieState>> upcoming;
        try {
            upcoming = Optional.ofNullable(
                    liturgieService.getNextDutyLiturgies(person.getId(), System.currentTimeMillis())
            ).orElseGet(List::of);
        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Konnte nächste Liturgien nicht laden", e
            );
        }

        final List<Map<String, String>> data = upcoming.stream()
                .map(pair -> toGottesdienstItem(pair.getFirst(), pair.getSecond()))
                .toList();

        return ApiResponseHelper.ok(data, request, request.getLocale());
    }

    // -----------------------------------------------------------
    // ALL
    // -----------------------------------------------------------
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> all(HttpServletRequest request) {
        final Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        final List<Pair<Liturgie, LiturgieState>> liturgies;
        try {
            liturgies = Optional.ofNullable(liturgieService.getNextLiturgies(person))
                    .orElseGet(List::of);
        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Konnte alle Liturgien nicht laden", e
            );
        }

        final List<Map<String, String>> data = liturgies.stream()
                .map(pair -> toGottesdienstItem(pair.getFirst(), pair.getSecond()))
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
        try {
            Liturgie liturgie = liturgieService.getLiturgie(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

            Map<String, Object> res = new LinkedHashMap<>(6);
            res.put("id", id);
            res.put("title", Optional.ofNullable(liturgie.getLiturgieType())
                    .map(LiturgieType::getLabel).orElse(""));
            res.put("date", Optional.ofNullable(liturgie.getDate())
                    .map(CMSDate::getEnglishDate).orElse(""));
            res.put("time", Optional.ofNullable(liturgie.getDate())
                    .map(CMSDate::getTime).orElse(""));
            res.put("location", Optional.ofNullable(liturgie.getTenant())
                    .map(t -> t.getName()).orElse(""));
            res.put("description", String.valueOf(liturgie.getNumber()));

            return ApiResponseHelper.ok(res, request, request.getLocale());

        } catch (SQLException e) {
            log.error("DB error reading liturgy {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "database error");
        }
    }

    // -----------------------------------------------------------
    // TAB „Verfügbarkeit“
    // -----------------------------------------------------------
    @GetMapping(value = "/{id}/availability", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailability(
            HttpServletRequest request,
            @PathVariable("id") UUID id
    ) {
        final Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        try {
            Liturgie liturgie = liturgieService.getLiturgie(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

            List<Map<String, Object>> res = new ArrayList<>();
            for (Pair<PersonOverviewDTO, LiturgieState> p :
                    liturgieMappingService.getStateForLiturgy(user.getTenant(), liturgie.getLiturgieId())) {

                Map<String, Object> row = new LinkedHashMap<>(3);
                row.put("personId", p.getFirst().getId());
                row.put("name", p.getFirst().getFirstName() + " " + p.getFirst().getLastName());
                // im Frontend werden "AVAILABLE/DUTY/UNAVAILABLE" erwartet
                row.put("state", p.getSecond() != null ? p.getSecond().name() : "UNAVAILABLE");
                res.add(row);
            }

            return ApiResponseHelper.ok(res, request, request.getLocale());
        } catch (SQLException e) {
            log.error("DB error reading availability for {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "database error");
        }
    }

    // -----------------------------------------------------------
    // Mapper: jetzt mit "id" und konsistenter location (Tenant-Name)
    // -----------------------------------------------------------
    private Map<String, String> toGottesdienstItem(Liturgie l, LiturgieState state) {
        Map<String, String> map = new LinkedHashMap<>(7);
        map.put("id", Optional.ofNullable(l.getLiturgieId()).map(UUID::toString).orElse(""));
        map.put("title", Optional.ofNullable(l.getLiturgieType()).map(LiturgieType::getLabel).orElse(""));
        map.put("date", Optional.ofNullable(l.getDate()).map(CMSDate::getGermanDate).orElse(""));
        map.put("time", Optional.ofNullable(l.getDate()).map(CMSDate::getTime).orElse(""));
        map.put("location", Optional.ofNullable(l.getTenant()).map(t -> t.getName()).orElse(""));
        map.put("state", state != null ? state.name() : "UNAVAILABLE");
        return map;
    }

    @PostMapping(value = "/{id}/unassign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> unassign(
            HttpServletRequest request,
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> payload
    ) {
        final Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        final String reason = Optional.ofNullable(payload.get("reason"))
                .map(Object::toString)
                .map(String::trim)
                .orElse("");
        if (reason.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason is required");
        }

        try {
            liturgieMappingService.unassignWithReason(person.getId(), id, reason);

            Map<String, String> res = new LinkedHashMap<>(1);
            res.put("status", "ok");
            return ApiResponseHelper.ok(res, request, request.getLocale());

        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "liturgy not found");
        } catch (SQLException e) {
            log.error("DB error unassigning user {} from liturgy {}: {}", person.getId(), id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "database error");
        }
    }
}
