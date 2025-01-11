package com.messdiener.cms.v3.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if(statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "error/error-401";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/error-403";
            }  else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/error-404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/error-500";
            } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                return "error/error-503";
            } else if (statusCode == HttpStatus.GATEWAY_TIMEOUT.value()) {
                return "error/error-504";
            }
        }
        return "error/error-403";
    }
}