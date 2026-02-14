package com.messdiener.cms.person.web.controller;

import com.messdiener.cms.domain.person.PersonCommandPort;
import com.messdiener.cms.shared.enums.PersonAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/personal/contact", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonContactController {

    private final PersonCommandPort personCommandPort;

    @PostMapping("/ai")
    public ResponseEntity<Map<String, String>> saveGeneral(
            @RequestParam("id") UUID id,
            @RequestParam("salutation") PersonAttributes.Salutation salutation,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("gender") PersonAttributes.Gender gender,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "mail", required = false) String mail
    ) {
        personCommandPort.updateGeneral(id, salutation, firstname, lastname, gender, phone, mobile, mail);
        return ok("Gespeichert.");
    }

    @PostMapping("/data")
    public ResponseEntity<Map<String, String>> saveDates(
            @RequestParam("id") UUID id,
            @RequestParam(value = "birthdate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            @RequestParam(value = "accessionDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate accessionDate,
            @RequestParam(value = "exitDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate exitDate
    ) {
        personCommandPort.updateDates(id, birthdate, accessionDate, exitDate);
        return ok("Gespeichert.");
    }

    @PostMapping("/account")
    public ResponseEntity<Map<String, String>> saveAccount(
            @RequestParam("id") UUID id,
            @RequestParam("type") PersonAttributes.Type type,
            @RequestParam("rank") PersonAttributes.Rank rank,
            @RequestParam("fRank") int fRank,
            @RequestParam("principal") UUID principal
    ) {
        personCommandPort.updateAccountAndManager(id, type, rank, fRank, principal);
        return ok("Gespeichert.");
    }

    @PostMapping("/address")
    public ResponseEntity<Map<String, String>> saveAddress(
            @RequestParam("id") UUID id,
            @RequestParam(value = "street", required = false) String street,
            @RequestParam(value = "houseNumber", required = false) String houseNumber,
            @RequestParam(value = "postalCode", required = false) String postalCode,
            @RequestParam(value = "city", required = false) String city
    ) {
        personCommandPort.updateAddress(id, street, houseNumber, postalCode, city);
        return ok("Gespeichert.");
    }

    @PostMapping("/bank")
    public ResponseEntity<Map<String, String>> saveBank(
            @RequestParam("id") UUID id,
            @RequestParam(value = "iban", required = false) String iban,
            @RequestParam(value = "bic", required = false) String bic,
            @RequestParam(value = "bank", required = false) String bank,
            @RequestParam(value = "accountHolder", required = false) String accountHolder
    ) {
        personCommandPort.updateBank(id, iban, bic, bank, accountHolder);
        return ok("Gespeichert.");
    }

    private ResponseEntity<Map<String, String>> ok(String msg) {
        return ResponseEntity.ok(Map.of("message", msg));
    }
}
