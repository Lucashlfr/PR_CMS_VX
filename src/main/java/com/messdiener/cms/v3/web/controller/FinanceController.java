package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.finance.TransactionEntry;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.planer.entities.PlanerText;
import com.messdiener.cms.v3.app.entities.planer.entities.PlannerLog;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.finance.FinanceService;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.Finance;
import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
import com.messdiener.cms.v3.shared.enums.tasks.LogType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class FinanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceController.class);
    private final Cache cache;
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;

    @PostConstruct
    public void init() {
        LOGGER.info("FinanceController initialized.");
    }

    @GetMapping("/finance")
    public String finance(HttpSession httpSession, Model model,
                          @RequestParam("q") Optional<String> q,
                          @RequestParam("id") Optional<String> idS,
                          @RequestParam("t") Optional<String> text) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);
        if (!personHelper.hasPermission(user, "TEAM")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");
        }

        var financeQueryService = cache.getFinanceQueryService();
        UUID tenantId = user.getTenantId();

        model.addAttribute("entries_all", financeQueryService.getEntriesByPerson(user.getId(), tenantId));
        model.addAttribute("summCash", Utils.roundToTwoDecimalPlaces(financeQueryService.getSumm(tenantId, "CASH")));
        model.addAttribute("summAccount", Utils.roundToTwoDecimalPlaces(financeQueryService.getSumm(tenantId, "ACCOUNT")));
        addText(model, text);

        String query = q.orElse("null");

        if (query.equals("info") && idS.isPresent()) {
            UUID id = UUID.fromString(idS.get());
            TransactionEntry entry = financeQueryService.getEntry(id).orElseThrow();
            model.addAttribute("transaction", entry);
            return "finance/financeInterface";
        }

        if (query.equals("overviewCash") || query.equals("overviewAccount")) {
            boolean isCash = query.equals("overviewCash");
            String type = isCash ? "CASH" : "ACCOUNT";
            model.addAttribute("summ", Utils.roundToTwoDecimalPlaces(financeQueryService.getSumm(tenantId, type)));
            model.addAttribute("transactions", financeQueryService.getEntriesByTenant(tenantId, type));
            model.addAttribute("current", isCash ? "cash" : "account");
            model.addAttribute("name", isCash ? Finance.CashRegisterType.CASH.getText() : Finance.CashRegisterType.ACCOUNT.getText());
            model.addAttribute("expense", financeQueryService.getSumm(tenantId, type, false));
            model.addAttribute("revenue", financeQueryService.getSumm(tenantId, type, true));
            return "finance/financeOverview";
        }

        double summ = Utils.roundToTwoDecimalPlaces(financeQueryService.getSumm(tenantId, "ACCOUNT") +
                financeQueryService.getSumm(tenantId, "CASH"));
        model.addAttribute("summ", summ);
        model.addAttribute("transactions", financeQueryService.getEntriesByTenant(tenantId));
        model.addAttribute("current", "overview");
        model.addAttribute("name", "Gesamt");
        model.addAttribute("expense", financeQueryService.getSumm(tenantId, "CASH", false) +
                financeQueryService.getSumm(tenantId, "ACCOUNT", false));
        model.addAttribute("revenue", financeQueryService.getSumm(tenantId, "CASH", true) +
                financeQueryService.getSumm(tenantId, "ACCOUNT", true));
        return "finance/financeOverview";
    }

    private void addText(Model model, Optional<String> t) {
        model.addAttribute("textShow", t.isPresent());
        String text = switch (t.orElse("null")) {
            case "fileSuccess" -> "Datei wurde erfolgreich hochgeladen";
            case "fileDelete" -> "Datei wurde erfolgreich gelöscht";
            case "saveSuccess" -> "Antrag wurde erfolgreich gespeichert";
            case "checkSuccess" -> "Beleg-Check wurde erfolgreich gespeichert";
            default -> "null";
        };
        model.addAttribute("text", text);
    }

    @GetMapping("/finance/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam("uuid") String uuid) throws IOException {
        File file = getFile(uuid).orElseThrow(() -> new FileNotFoundException("File not found for ID: " + uuid));
        Path path = file.toPath();
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            LOGGER.warn("File not found or unreadable: {}", path);
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private Optional<File> getFile(String id) {
        File directory = Paths.get("cms_vx", "financeData").toFile();
        return Optional.ofNullable(directory.listFiles())
                .stream()
                .flatMap(Arrays::stream)
                .filter(file -> file.getName().startsWith(id))
                .findFirst();
    }

    @PostMapping("/finance/save")
    public RedirectView save(@RequestParam("documentType") String documentType,
                             @RequestParam("type") String type,
                             @RequestParam("description") String description,
                             @RequestParam("value") double value,
                             @RequestParam("date") String englishDate,
                             @RequestParam("costCenter") String costCenter,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam("notes") String notes,
                             @RequestParam("id") Optional<String> id,
                             @RequestParam("planerId") Optional<String> planerId) throws SQLException {

        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        TransactionEntry financeEntry = id.map(i -> {
            try {
                return cache.getFinanceQueryService().getEntry(UUID.fromString(i))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + i));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).orElseGet(() -> TransactionEntry.empty(person.getTenantId(), person.getId()));

        financeEntry.setType(type);
        financeEntry.setDescription(description);
        financeEntry.setValue(calculateValue(documentType, value));
        financeEntry.setExchange("EXCHANGE".equals(documentType));
        financeEntry.setDocumentDate(CMSDate.convert(englishDate, DateUtils.DateType.ENGLISH));
        financeEntry.setCostCenter(costCenter);
        financeEntry.setNote(notes);
        planerId.ifPresent(s -> financeEntry.setPlanerId(Optional.of(UUID.fromString(s))));

        if (!file.isEmpty()) {
            try {
                saveFile(file, financeEntry.getId().toString());
            } catch (IOException e) {
                LOGGER.error("File upload failed", e);
                return new RedirectView("/finance?q=error&t=fileUploadFailed");
            }
        }

        cache.getFinanceService().saveFinanceRequest(financeEntry);

        if (planerId.isPresent()) {
            UUID planerUUID = UUID.fromString(planerId.get());
            PlannerLog plannerLog = new PlannerLog(
                    UUID.randomUUID(),
                    PlanerTag.BUDGET,
                    LogType.INFO,
                    CMSDate.current(),
                    "Update",
                    person.getName() + " hat folgende Abrechnung erstellt: " + description
            );

            try {
                cache.getPlannerLogService().createLog(UUID.fromString(planerId.get()), plannerLog);
                cache.getPlannerTextService().saveText(UUID.fromString(planerId.get()), PlanerText.create(person.getId(), PlanerTag.BUDGET, "STATE", "ok"));
            } catch (SQLException e) {
                LOGGER.error("Failed to save planner log or planner text for PlanerID: {}", planerUUID, e);
                return new RedirectView("/finance?q=error&t=planerSaveError");
            }
        }


        return planerId.map(s -> new RedirectView("/planer?q=BUDGET&id=" + s + "&state=ok"))
                .orElseGet(() -> new RedirectView("/finance?q=info&id=" + financeEntry.getId() + "&t=saveSuccess"));
    }

    private double calculateValue(String documentType, double value) {
        return switch (documentType) {
            case "EXPENSE" -> -value;
            case "REVENUE", "EXCHANGE" -> value;
            default -> throw new IllegalStateException("Unexpected value: " + documentType);
        };
    }

    private void saveFile(MultipartFile file, String id) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains(".")) ? originalName.substring(originalName.lastIndexOf(".")) : "";

        Path dirPath = Paths.get("cms_vx", "financeData");
        Files.createDirectories(dirPath);

        Path filePath = dirPath.resolve(id + extension);
        Files.write(filePath, file.getBytes());
    }
}
