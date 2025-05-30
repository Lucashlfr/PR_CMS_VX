package com.messdiener.cms.v3.web.controller.file;

import com.messdiener.cms.v3.app.entities.document.Document;
import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.exception.StorageException;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityHelper securityHelper;
    private final StorageService storageService;
    private final PersonHelper personHelper;

    @GetMapping("/cloud")
    public String cloud(HttpSession session, Model model) throws SQLException {
        securityHelper.addPersonToSession(session);

        model.addAttribute("id", Cache.WEBSITE);
        model.addAttribute("files", storageService.loadFiles(Cache.WEBSITE));
        model.addAttribute("personHelper", personHelper);

        return "cloud/list/cloudList";
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
}
