package com.messdiener.cms.v3.web.controller.audit;


import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final SecurityHelper securityHelper;


    @PostMapping("/audit/create")
    public RedirectView createAudit(@RequestParam("connectedId") UUID id, @RequestParam("title") String title, @RequestParam("category")String category,
            @RequestParam("description") String description, @RequestHeader(value = "Referer", required = false) String referer) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        auditService.createLog(new AuditLog(UUID.randomUUID(), MessageType.COMMENT, ActionCategory.valueOf(category), id, user.getId(), CMSDate.current(), title, description, MessageInformationCascade.C0, false));

        return new RedirectView(referer != null ? referer : "/defaultZiel");
    }

}
