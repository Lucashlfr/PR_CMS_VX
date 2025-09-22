package com.messdiener.cms.web.common.security;

import com.messdiener.cms.domain.auth.AuthContextPort;
import com.messdiener.cms.domain.person.PersonLookupPort;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.domain.workflow.WorkflowQueryPort;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityHelper.class);

    private final AuthContextPort authContextPort;
    private final PersonLookupPort personLookupPort;
    private final WorkflowQueryPort workflowQueryPort;

    public String getUsername() {
        return authContextPort.getUsername();
    }

    public Optional<PersonSessionView> getPerson() {
        try {
            final String username = getUsername();
            return (username == null) ? Optional.empty()
                    : personLookupPort.findByUsernameForSession(username);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch person by username: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<PersonSessionView> addPersonToSession(HttpSession httpSession) {
        try {
            Optional<PersonSessionView> personOpt = getPerson();

            personOpt.ifPresent(p -> {
                httpSession.setAttribute("sessionUser", p); // statt Entity: die SessionView
                httpSession.setAttribute("serviceName", "CMSX");
                httpSession.setAttribute("tenantName", p.tenantName());
                httpSession.setAttribute("img", p.imgAddress());
                httpSession.setAttribute("openWorkflows", workflowQueryPort.countRelevantWorkflows(p.id().toString()));
                httpSession.setAttribute("fRank", p.fRank());
            });

            return personOpt;
        } catch (Exception e) {
            LOGGER.warn("Session update failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
