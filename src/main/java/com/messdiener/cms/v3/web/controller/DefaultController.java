package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.privacy.PrivacyPolicy;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DefaultController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultController.class);
    private final Cache cache;
    private final SecurityHelper securityHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("DefaultController initialized.");
    }

    @GetMapping("/dashboard")
    public String index(HttpSession httpSession, Model model) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);
        model.addAttribute("user", user);
        model.addAttribute("tenants", cache.getTenantService().getTenants());
        if(user.isCustomPassword()) {
            return "index";
        }
        return "security/customPw";
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam("q") String q) throws SQLException, FileNotFoundException {
        Optional<File> fileOpt = getFile(q);

        if (fileOpt.isEmpty()) {
            LOGGER.warn("Requested file not found: {}", q);
            throw new FileNotFoundException("Datei nicht gefunden");
        }

        File file = fileOpt.get();
        try (InputStream inputStream = new FileInputStream(file)) {
            InputStreamResource resource = new InputStreamResource(inputStream);
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            LOGGER.error("Error while reading file: {}", file.getAbsolutePath(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fehler beim Datei-Download");
        }
    }

    public Optional<File> getFile(String q) {
        File file = new File(q);
        return file.exists() && file.isFile() ? Optional.of(file) : Optional.empty();
    }

    @GetMapping("/static")
    public RedirectView staticR() {
        return new RedirectView("/");
    }

    @GetMapping("/public")
    public RedirectView publicR() {
        return new RedirectView("/");
    }

    @GetMapping("/public/privacyPolicy")
    public String privacyPolicyR() {
        return "privacy_policy";
    }

    @PostMapping("/public/privacy/submit")
    public RedirectView privacySubmit(
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("street") String street,
            @RequestParam("number") String number,
            @RequestParam("plz") String plz,
            @RequestParam("town") String town,
            @RequestParam("signature") String signature,
            @RequestParam("option1") Optional<String> option1,
            @RequestParam("option2") Optional<String> option2,
            @RequestParam("option3") Optional<String> option3,
            @RequestParam("option4") Optional<String> option4,
            @RequestParam("option5") Optional<String> option5,
            @RequestParam("option6") Optional<String> option6,
            @RequestParam("option7") Optional<String> option7
    ) throws SQLException, IOException, IllegalAccessException {

        PrivacyPolicy privacyPolicy = new PrivacyPolicy(
                UUID.randomUUID(),
                CMSDate.current(),
                firstname, lastname, street, number, plz, town,
                option1.isPresent(), option2.isPresent(), option3.isPresent(), option4.isPresent(),
                option5.isPresent(), option6.isPresent(), option7.isPresent(),
                signature
        );

        cache.getPrivacyService().create(privacyPolicy);
        return new RedirectView("/");
    }
}
