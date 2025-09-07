package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.security.SecurityHelper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private final AppConfiguration appConfiguration;
    private final DatabaseService databaseService;
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    private final SecurityHelper securityHelper;
    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        LOGGER.info("LoginController initialized.");
    }

    @GetMapping("/login")
    public String getDefault(HttpSession httpSession) {

        if (httpSession.getAttribute("serviceName") == null) {
            httpSession.setAttribute("serviceName", appConfiguration.getValue("serviceName"));
        }
        return "security/login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "security/login";
    }

    @PostMapping("/auth/changePassword")
    public RedirectView customPw(
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes
    ) throws SQLException {

        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Das Passwort muss mindestens 8 Zeichen lang sein.");
            return new RedirectView("/security/customPw");
        }

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        user.setPassword(password);
        user.setCustomPassword(true);
        personService.updatePerson(user);
        return new RedirectView("/dashboard");
    }

    @GetMapping("/security/customPw")
    public String pw(@ModelAttribute("error") String error, Model model, HttpSession httpSession) {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);
        model.addAttribute("user", user);
        model.addAttribute("error", error); // nur nötig, wenn du möchtest
        return "security/customPw";
    }


}
