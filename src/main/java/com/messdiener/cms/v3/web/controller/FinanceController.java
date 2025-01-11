package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.finance.TransactionEntry;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.Finance;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Controller
public class FinanceController {

    @GetMapping("/finance")
    public String finance(HttpSession httpSession, Model model, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> idS, @RequestParam("t") Optional<String> text) throws SQLException {

        Person user = SecurityHelper.addPersonToSession(httpSession);
        model.addAttribute("entries_all", Cache.getFinanceService().getEntriesByPerson(user));


        model.addAttribute("summCash", Utils.roundToTwoDecimalPlaces(Cache.getFinanceService().getSumm(user.getTenant(), "CASH")));
        model.addAttribute("summAccount", Utils.roundToTwoDecimalPlaces(Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT")));

        addText(model, text);

        String query = q.orElse("null");

        if (query.equals("overviewCash")) {
            model.addAttribute("summ", Utils.roundToTwoDecimalPlaces(Cache.getFinanceService().getSumm(user.getTenant(), "CASH")));
            model.addAttribute("transactions", Cache.getFinanceService().getEntriesByTenant(user.getTenantId(), "CASH"));
            model.addAttribute("current", "cash");
            model.addAttribute("name", Finance.CashRegisterType.CASH.getText());
            model.addAttribute("expense", Cache.getFinanceService().getSumm(user.getTenant(), "CASH", false));
            model.addAttribute("revenue", Cache.getFinanceService().getSumm(user.getTenant(), "CASH", true));
            return "finance/financeOverview";
        } else if (query.equals("overviewAccount")) {
            model.addAttribute("summ", Utils.roundToTwoDecimalPlaces(Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT")));
            model.addAttribute("transactions", Cache.getFinanceService().getEntriesByTenant(user.getTenantId(), "ACCOUNT"));
            model.addAttribute("current", "account");
            model.addAttribute("name", Finance.CashRegisterType.ACCOUNT.getText());
            model.addAttribute("expense", Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT", false));
            model.addAttribute("revenue", Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT", true));
            return "finance/financeOverview";
        } else {
            double summ = Utils.roundToTwoDecimalPlaces(Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT") + Cache.getFinanceService().getSumm(user.getTenant(), "CASH"));
            model.addAttribute("summ", summ);
            model.addAttribute("transactions", Cache.getFinanceService().getEntriesByTenant(user.getTenantId()));
            model.addAttribute("current", "overview");
            model.addAttribute("name", "Gesamt");

            model.addAttribute("expense", Cache.getFinanceService().getSumm(user.getTenant(), "CASH", false) + Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT", false));
            model.addAttribute("revenue", Cache.getFinanceService().getSumm(user.getTenant(), "CASH", true) + Cache.getFinanceService().getSumm(user.getTenant(), "ACCOUNT", true));

            return "finance/financeOverview";
        }
    }

    private void addText(Model model, Optional<String> t) {
        String text;
        model.addAttribute("textShow", t.isPresent());
        switch (t.orElse("null")) {
            case "fileSuccess":
                text = "Datei wurde erfolgreich hochgeladen";
                break;
            case "fileDelete":
                text = "Datei wurde erfolgreich gelöscht";
                break;
            case "saveSuccess":
                text = "Antrag wurde erfolgreich gespeichert";
                break;
            case "checkSuccess":
                text = "Beleg-Check wurde erfolgreich gespeichert";
                break;
            default:
                text = "null";
        }
        model.addAttribute("text", text);
    }


    @GetMapping("/finance/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam("uuid") UUID uuid, @RequestParam("name") String name) throws IOException {
        // Annahme: Dateiname ist der ID-Wert gefolgt von ".txt" (zum Beispiel: 123.txt)
        File file = getFile(uuid, name).orElseThrow();
        Resource resource = new UrlResource(Path.of(file.getPath()).toUri());

        if (resource.exists() && resource.isReadable()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + file.getName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } else {
            // Datei nicht gefunden oder nicht lesbar
            // Hier könntest du eine Fehlerseite anzeigen oder eine entsprechende Antwort zurückgeben
            return ResponseEntity.notFound().build();
        }
    }

    public Optional<File> getFile(UUID id, String filename) {
        if (!new File("." + File.separator + "cms_v2" + File.separator + "financeData" + File.separator + id).exists())
            return Optional.empty();

        return Stream.of((new File("." + File.separator + "cms_v2" + File.separator + "financeData" + File.separator + id).listFiles()))
                .filter(file -> file.getName().equals(filename))
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
                             @RequestParam("id") Optional<String> id) throws SQLException {

        Person person = SecurityHelper.getPerson();
        TransactionEntry financeEntry = TransactionEntry.empty(person);
        if (id.isPresent()) {
            financeEntry = Cache.getFinanceService().getEntry(UUID.fromString(id.get())).orElseThrow();
        }

        System.out.println("/save");
        System.out.println(person.getTenantId());
        System.out.println(type);

        financeEntry.setType(type);
        financeEntry.setDescription(description);

        switch (documentType) {
            case "EXPENSE":
                financeEntry.setValue(-value);
                break;

            case "REVENUE":
                financeEntry.setValue(value);
                break;

            case "EXCHANGE":
                financeEntry.setValue(value);
                financeEntry.setExchange(true);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + documentType);
        }

        financeEntry.setDocumentDate(CMSDate.convert(englishDate, DateUtils.DateType.ENGLISH));
        financeEntry.setCostCenter(costCenter);
        financeEntry.setNote(notes);

        try {
            // Originaler Dateiname und Endung extrahieren
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // Zielname erstellen: id + Endung
            String newFileName = financeEntry.getId() + extension;

            // Byte-Daten der Datei auslesen
            byte[] bytes = file.getBytes();

            // Verzeichnis erstellen, falls nicht vorhanden
            File dir = new File("." + File.separator + "cms_v2" + File.separator + "financeData");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Datei im Zielverzeichnis speichern
            File serverFile = new File(dir.getAbsolutePath() + File.separator + newFileName);
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
                stream.write(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace(); // Fehlerbehandlung
        }


        financeEntry.save();
        return new RedirectView("/finance?q=info&id=" + id + "&t=saveSuccess");
    }

}
