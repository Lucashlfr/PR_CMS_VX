package com.messdiener.cms.v3.web;


import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.audit.ComplianceService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.scheduler.GlobalManager;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

@Controller
@RequiredArgsConstructor
public class HildeGuardController {

    private final AuditService auditService;
    private final SecurityHelper securityHelper;
    private final ComplianceService complianceService;
    private final PersonHelper personHelper;
    private final GlobalManager globalManager;


    @GetMapping("/guard")
    public String guard(HttpSession session, Model model)  throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(session);

        globalManager.startUp();

        model.addAttribute("checks", complianceService.getCompliance(user.getFRank(), user.getTenantId()));
        model.addAttribute("personHelper", personHelper);

        return "guard";
    }

}
