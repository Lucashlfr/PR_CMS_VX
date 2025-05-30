package com.messdiener.cms.v3.web.controller.event;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/event/application")
public class ApplicationController {

    // Verarbeitet multipart/form-data POSTs
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveApplication(MultipartHttpServletRequest request) throws IOException {
        // 1. Alle normalen Felder auslesen
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> formValues = new HashMap<>();
        parameterMap.forEach((name, values) -> {
            // Wenn mehrere Werte, durch Komma trennen
            String joined = String.join(",", values);
            formValues.put(name, joined);
        });

        // 2. Alle File-Uploads verarbeiten
        Iterator<String> fileNames = request.getFileNames();
        String uploadDir = "/pfad/zum/upload/verzeichnis";  // anpassen!
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        while (fileNames.hasNext()) {
            String fieldName = fileNames.next();
            MultipartFile file = request.getFile(fieldName);
            if (file != null && !file.isEmpty()) {
                // Original-Dateiname
                String originalFilename = Objects.requireNonNull(file.getOriginalFilename())
                        .replaceAll("[^a-zA-Z0-9\\.\\-\\_]", "_");
                File dest = new File(uploadFolder, originalFilename);
                file.transferTo(dest);
                // Optional: Pfad ins formValues-Map speichern
                formValues.put(fieldName, dest.getAbsolutePath());
            }
        }

        // 3. Hier könntest du formValues z.B. in die Datenbank schreiben
        //    Example: applicationService.save(formValues);

        // 4. Rückgabe JSON mit redirectUrl
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", "/go?type=application&id=" + request.getParameter("id"));
        return ResponseEntity.ok(response);
    }
}

