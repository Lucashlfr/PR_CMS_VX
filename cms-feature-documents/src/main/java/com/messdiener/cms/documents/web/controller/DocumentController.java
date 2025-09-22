package com.messdiener.cms.documents.web.controller;

import com.messdiener.cms.documents.app.service.StorageService;
import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.documents.persistence.service.DocumentService;
import com.messdiener.cms.documents.domain.entity.Document;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.utils.time.CMSDate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

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

        model.addAttribute("id", Cache.WEBSITE);
        model.addAttribute("files", storageService.loadFiles(Cache.WEBSITE));

        return "cloud/list/cloudList";
    }

    @PostMapping("/document/create")
    public RedirectView createDocument(
            HttpServletRequest request,
            @RequestParam("target") UUID target,
            @RequestParam("title") String title
    ) throws SQLException {
        UUID ownerId = securityHelper.getPerson()
                .map(PersonSessionView::id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Document document = new Document(
                UUID.randomUUID(),
                ownerId,
                target,
                CMSDate.current(),
                title,
                "",
                ""
        );
        documentService.saveDocument(document);

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return new RedirectView(referer);
        }
        return new RedirectView("/");
    }

    @PostMapping("/document/update")
    public String save(
            @RequestParam("id") UUID id,
            @RequestParam("owner") UUID owner,
            @RequestParam("target") UUID target,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("permissions") String permissions
    ) throws SQLException {

        Document document = new Document(id, owner, target, CMSDate.current(), title, content, permissions);
        documentService.saveDocument(document);
        return "close-popup";
    }

    @GetMapping("/document")
    public String document(Model model, @RequestParam("id") UUID id) throws SQLException {
        Document document = documentService.getDocument(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Document not found"));
        model.addAttribute("document", document);
        return "document/document";
    }
}
