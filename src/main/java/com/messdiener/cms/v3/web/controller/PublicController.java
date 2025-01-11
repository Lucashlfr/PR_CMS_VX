package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.security.SecurityHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;

@Controller
public class PublicController {


    @GetMapping("/publicController")
    public String publicController(HttpSession session, Model model) throws SQLException {

        Person person = SecurityHelper.addPersonToSession(session);
        model.addAttribute("person", person);

        return "public/publicIndex";
    }


}
