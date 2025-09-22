package com.messdiener.cms.auth.web.controller;

import com.messdiener.cms.web.common.security.SecurityHelper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SFixOController {

    private final SecurityHelper securityHelper;


    @GetMapping("/sFixO")
    public String sFixO(HttpSession httpSession) {
        securityHelper.addPersonToSession(httpSession);
        return "sFixO/sFixO";
    }

}
