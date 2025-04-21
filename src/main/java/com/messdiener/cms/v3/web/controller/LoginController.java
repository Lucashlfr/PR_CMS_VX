package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.SQLException;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private final AppConfiguration appConfiguration;
    private final DatabaseService databaseService;
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

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
}
