package com.messdiener.cms.auth.app.adapters;

import com.messdiener.cms.domain.auth.AuthContextPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuthContextAdapter implements AuthContextPort {
    @Override
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
}
