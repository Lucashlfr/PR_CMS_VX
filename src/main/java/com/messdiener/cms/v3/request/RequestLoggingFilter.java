package com.messdiener.cms.v3.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        StatusCaptureResponseWrapper responseWrapper = new StatusCaptureResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        int status = responseWrapper.getStatus();
        String method = request.getMethod();
        String path = request.getRequestURI();
        LocalDateTime timestamp = LocalDateTime.now();

        if(path.startsWith("/dist") || path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/img") || path.startsWith("/DataTables") || path.startsWith("/file") || path.startsWith("/fonts")) {
            if(status == 200)
                return;
        }

        // Optional: Persistieren oder in Log-Liste einfügen
        System.out.printf("[%s] %s %s %s -> %d%n", timestamp, method, path, getFullURL(request), status);
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        return queryString == null ? requestURL.toString() : requestURL.append('?').append(queryString).toString();
    }
}
