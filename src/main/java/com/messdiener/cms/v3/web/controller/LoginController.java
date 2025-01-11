package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.shared.cache.Cache;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.SQLException;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String getDefault(HttpSession httpSession) throws SQLException {

        if(httpSession.getAttribute("serviceName") == null){
            httpSession.setAttribute("serviceName", Cache.APP_NAME);
        }

        if(System.currentTimeMillis() > Cache.getRefresh()){
            Cache.getDatabaseService().reconnect();
        }


        return "security/login";
    }

    // Login form with error
    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "security/login";
    }

}
