package com.messdiener.cms.v3.security.login;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.security.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomPasswordInterceptor implements HandlerInterceptor {

    private final SecurityHelper securityHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        // Ausnahmen erlauben
        if (uri.startsWith("/auth/changePassword") || uri.startsWith("/login") || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img")) {
            return true;
        }

        Optional<Person> personOpt = securityHelper.getPerson();
        if (personOpt.isEmpty()) {
            return true; // nicht angemeldet
        }

        Person person = personOpt.get();
        if (!person.isCustomPassword()) {
            // Nur redirecten, wenn wir NICHT schon auf der CustomPw-Seite sind
            response.sendRedirect("/security/customPw");
            return false;
        }

        return true;
    }
}
