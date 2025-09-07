package com.messdiener.cms.v3.web.controller.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.v3.app.services.event.EventApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final EventApplicationService eventApplicationService;

    @PostMapping(value = "/application/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveApplication(MultipartHttpServletRequest request) throws IOException, SQLException {

        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> formValues = new HashMap<>();
        parameterMap.forEach((name, values) -> {
            String joined = String.join(",", values);
            formValues.put(name, joined);
        });

        Map<String, String> response = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(formValues);

        UUID id = UUID.fromString(request.getParameter("id"));
        eventApplicationService.saveResult(UUID.randomUUID(), id, json, "#");

        response.put("redirectUrl", "/go?id=" + id + "&state=success");
        return ResponseEntity.ok(response);
    }
}

