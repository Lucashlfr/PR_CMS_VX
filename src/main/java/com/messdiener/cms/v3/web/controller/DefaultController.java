package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.privacy.PrivacyPolicy;
import com.messdiener.cms.v3.app.request.ImportRequest;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class DefaultController {

    @GetMapping("/")
    public String index(HttpSession httpSession, Model model) throws SQLException {

        Person user = SecurityHelper.addPersonToSession(httpSession);
        model.addAttribute("user", user);
        model.addAttribute("tenants", Cache.getTenantService().getTenants());

        return "index";
    }

    @PostMapping("/tenant/change")
    public RedirectView changeTenant(@RequestParam("tenantId") UUID tenantId) throws SQLException {

        Person user = SecurityHelper.getPerson();
        user.setTenantId(tenantId);
        user.update();

        return new RedirectView("/");
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam("q") String q) throws SQLException, FileNotFoundException {

        File file = getFile(q).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

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
    }

    public Optional<File> getFile(String q){
        return Optional.of(new File(q));
    }

    @GetMapping("/static")
    public RedirectView staticR(){
        return new RedirectView("/");
    }

    @GetMapping("/public")
    public RedirectView publicR(){
        return new RedirectView("/");
    }

    @PostMapping("/import")
    @ResponseBody
    public String importData(@RequestParam("file") MultipartFile file) throws SQLException, IOException, IllegalAccessException {

        List<List<String>> records = Utils.importFromCSV(file);
        ImportRequest request = ImportRequest.createRequest(records);
        return request.importFunction();
    }

    @GetMapping("/public/privacyPolicy")
    public String privacyPolicyR(){
        return "privacy_policy";
    }

    @PostMapping("/public/privacy/submit")
    public RedirectView privacySubmit(@RequestParam("firstname")String firstname, @RequestParam("lastname")String lastname,
                                      @RequestParam("street")String street, @RequestParam("number")String number,
                                      @RequestParam("plz")String plz, @RequestParam("town")String town,
                                      @RequestParam("signature")String signature,
                                      @RequestParam("option1") Optional<String> option1, @RequestParam("option2") Optional<String> option2,
                                      @RequestParam("option3") Optional<String> option3, @RequestParam("option4") Optional<String> option4,
                                      @RequestParam("option5") Optional<String> option5, @RequestParam("option6") Optional<String> option6,
                                      @RequestParam("option7") Optional<String> option7) throws SQLException, IOException, IllegalAccessException {

        PrivacyPolicy privacyPolicy = new PrivacyPolicy(UUID.randomUUID(), CMSDate.current(), firstname, lastname, street, number, plz, town, option1.isPresent(), option2.isPresent(), option3.isPresent(), option4.isPresent(),
                option5.isPresent(), option6.isPresent(), option7.isPresent(), signature);
        Cache.getPrivacyService().create(privacyPolicy);

        return new RedirectView("/");
    }

}
