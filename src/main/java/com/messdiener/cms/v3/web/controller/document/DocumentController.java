package com.messdiener.cms.v3.web.controller.document;

import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.entities.document.Document;
import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.finance.Transaction;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.exception.StorageException;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityHelper securityHelper;
    private final StorageService storageService;

    @GetMapping("/cloud")
    public String cloud(HttpSession session, Model model) throws SQLException {
        securityHelper.addPersonToSession(session);

        model.addAttribute("files", storageService.loadFiles(Cache.WEBSITE));

        return "cloud/list/cloudList";
    }

    @PostMapping(value = "/cloud/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RedirectView saveTransaction(
            @RequestParam("title") String title,
            @RequestParam("img") MultipartFile img,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        storageService.store(img, new StorageFile(UUID.randomUUID(), 0, user.getId(), Cache.WEBSITE, CMSDate.current(), title, CMSDate.current(), 10));

        redirectAttributes.addFlashAttribute("success", "Transaktion erfolgreich gespeichert.");
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/";
        }
        return new RedirectView(referer);
    }


    @PostMapping("/document/create")
    public RedirectView createDocument(HttpServletRequest request, @RequestParam("target") UUID target, @RequestParam("title") String title) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Document document = new Document(UUID.randomUUID(), user.getId(), target, CMSDate.current(), title, "", "");
        documentService.saveDocument(document);

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return new RedirectView(referer);
        }

        // Fallback falls kein Referer mitgegeben wurde
        return new RedirectView("/");
    }

    @PostMapping("/document/update")
    public String save(HttpServletRequest request, @RequestParam("id") UUID id, @RequestParam("owner") UUID owner,
                       @RequestParam("target") UUID target, @RequestParam("title") String title,
                       @RequestParam("content") String content, @RequestParam("permissions") String permissions) throws SQLException {

        Document document = new Document(id, owner, target, CMSDate.current(), title, content, permissions);
        documentService.saveDocument(document);

        return "close-popup";

    }

    @GetMapping("/document")
    public String document(Model model, @RequestParam("id") UUID id) throws SQLException {
        Document document = documentService.getDocument(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Document not found"));
        model.addAttribute("document", document);
        return "document/document";
    }


    @GetMapping("/storage/file")
    public ResponseEntity<InputStreamResource> getFile(
            @RequestParam("id") UUID id,
            @RequestParam(name = "download", defaultValue = "false") boolean downloadFlag) {

        // Datei laden
        File file;
        try {
            file = storageService.load(id.toString());
        } catch (StorageException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Datei nicht gefunden", e);
        }

        // InputStreamResource vorbereiten
        InputStreamResource resource;
        try {
            resource = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Datei nicht gefunden", e);
        }

        // MIME-Type ermitteln
        MediaType mediaType;
        try {
            String mime = Files.probeContentType(file.toPath());
            mediaType = (mime != null)
                    ? MediaType.parseMediaType(mime)
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // Content-Disposition: entweder inline oder attachment
        String dispositionType = downloadFlag ? "attachment" : "inline";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                dispositionType + "; filename=\"" + file.getName() + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

}
