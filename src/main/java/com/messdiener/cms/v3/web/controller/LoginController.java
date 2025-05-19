package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
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

    @PostConstruct
    public void init() {
        LOGGER.info("LoginController initialized.");
    }

    @GetMapping("/login")
    public String getDefault(HttpSession httpSession) {

        if (httpSession.getAttribute("serviceName") == null) {
            httpSession.setAttribute("serviceName", appConfiguration.getValue("serviceName"));
        }
/*
   // Reconnect only if necessary
        if (databaseService.getConnection() == null) {
            LOGGER.info("Database connection refresh triggered.");
            databaseService.reconnect();
        }
 */
        return "security/login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "security/login";
    }

    @PostMapping("/customPw")
    public RedirectView customPw(@RequestParam("password")String password, @RequestParam("password2")String password2) throws SQLException {
        if(password.equals(password2)) {
            Person user = securityHelper.getPerson()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            user.setPassword(password);
            user.setCustomPassword(true);
            personService.updatePerson(user);
            return new RedirectView("/dashboard");
        }
        return new RedirectView("/customPw");
    }
}
