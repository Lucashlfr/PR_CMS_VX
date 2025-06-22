package com.messdiener.cms.v3.web.controller.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.v3.app.services.event.EventApplicationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/application")
public class ApplicationController {

    private final EventApplicationService eventApplicationService;

    public ApplicationController(EventApplicationService eventApplicationService) {
        this.eventApplicationService = eventApplicationService;
    }

    // Verarbeitet multipart/form-data POSTs
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveApplication(MultipartHttpServletRequest request) throws IOException, SQLException {
        // 1. Alle normalen Felder auslesen
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> formValues = new HashMap<>();
        parameterMap.forEach((name, values) -> {
            // Wenn mehrere Werte, durch Komma trennen
            String joined = String.join(",", values);
            formValues.put(name, joined);
        });

        // 4. Rückgabe JSON mit redirectUrl
        Map<String, String> response = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(formValues);

        eventApplicationService.saveResult(UUID.randomUUID(), UUID.fromString(request.getParameter("id")), json, "#");

        response.put("redirectUrl", "/go?type=application&id=" + request.getParameter("id") + "&state=success");
        return ResponseEntity.ok(response);
    }
}

