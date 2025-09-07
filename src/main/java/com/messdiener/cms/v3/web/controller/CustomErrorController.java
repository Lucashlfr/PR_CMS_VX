package com.messdiener.cms.v3.web.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);

    @PostConstruct
    public void init() {
        LOGGER.info("CustomErrorController initialized.");
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            try {
                int statusCode = Integer.parseInt(status.toString());
                return resolveErrorView(statusCode);
            } catch (NumberFormatException ignored){
                //Ignored
            }
        }

        LOGGER.warn("No status code found. Redirecting to error-403.");
        return "error/error-403";
    }

    private String resolveErrorView(int statusCode) {
        return switch (statusCode) {
            case 401 -> "error/error-401";
            case 404 -> "error/error-404";
            case 500 -> "error/error-500";
            case 503 -> "error/error-503";
            case 504 -> "error/error-504";
            default -> "error/error-403";
        };
    }
}
