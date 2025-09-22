package com.messdiener.cms.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {


    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        StatusCaptureResponseWrapper responseWrapper = new StatusCaptureResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        int status = responseWrapper.getStatus();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if(path.startsWith("/dist") || path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/img") || path.startsWith("/DataTables") || path.startsWith("/file") || path.startsWith("/fonts")) {
            if(status == 200)
                return;
        }

        // Optional: Persistieren oder in Log-Liste einfügen
        LOGGER.info("{} {} {} {} -> {}",
                username,
                method,              // HTTP-Verb
                path,                // Servlet-Pfad
                getFullURL(request), // kompletter Request-URL
                status);             // HTTP-Statuscode
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        return queryString == null ? requestURL.toString() : requestURL.append('?').append(queryString).toString();
    }
}
