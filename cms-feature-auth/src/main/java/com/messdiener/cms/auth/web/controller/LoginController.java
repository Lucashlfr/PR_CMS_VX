package com.messdiener.cms.auth.web.controller;

import com.messdiener.cms.domain.auth.AuthContextPort;
import com.messdiener.cms.domain.person.PersonPasswordCommandPort;
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

@Controller
@RequiredArgsConstructor
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    private final AuthContextPort authContextPort;
    private final PersonPasswordCommandPort personPasswordCommandPort;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        LOGGER.info("LoginController initialized.");
    }

    @GetMapping("/login")
    public String getDefault() {
        return "security/login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "security/login";
    }

    @PostMapping("/auth/changePassword")
    public RedirectView customPw(@RequestParam("password") String password,
                                 RedirectAttributes redirectAttributes) {

        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Das Passwort muss mindestens 8 Zeichen lang sein.");
            return new RedirectView("/security/customPw");
        }

        String username = authContextPort.getUsername();
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        String encoded = passwordEncoder.encode(password);
        personPasswordCommandPort.setPasswordAndMarkCustom(username, encoded);
        return new RedirectView("/dashboard");
    }

    @GetMapping("/security/customPw")
    public String pw(@ModelAttribute("error") String error, Model model, HttpSession httpSession) {
        model.addAttribute("error", error);
        return "security/customPw";
    }
}
