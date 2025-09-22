package com.messdiener.cms.audit.web.controller;

import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.web.common.security.SecurityHelper;        // NEU
import com.messdiener.cms.domain.person.PersonSessionView;          // NEU
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageInformationCascade;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.utils.time.CMSDate;
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
    private final SecurityHelper securityHelper; // aus web-common

    @PostMapping("/audit/create")
    public RedirectView createAudit(@RequestParam("connectedId") UUID id,
                                    @RequestParam("title") String title,
                                    @RequestParam("category") String category,
                                    @RequestParam("description") String description,
                                    @RequestHeader(value = "Referer", required = false) String referer) throws SQLException {

        PersonSessionView user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        AuditLog log = new AuditLog(
                UUID.randomUUID(),
                MessageType.COMMENT,
                ActionCategory.valueOf(category),
                id,
                user.id(),
                CMSDate.current(),
                title,
                description,
                MessageInformationCascade.C0,
                false
        );

        auditService.createLog(log);

        return new RedirectView(referer != null ? referer : "/defaultZiel");
    }
}
