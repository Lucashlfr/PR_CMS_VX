package com.messdiener.cms.v3.app.helper.finance;

import com.itextpdf.text.DocumentException;
import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.finance.Budget;
import com.messdiener.cms.v3.app.entities.finance.Transaction;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.table.CMSCell;
import com.messdiener.cms.v3.app.entities.table.CMSRow;
import com.messdiener.cms.v3.app.export.FileCreator;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.finance.BudgetService;
import com.messdiener.cms.v3.app.services.finance.TransactionService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.shared.enums.finance.BudgetYear;
import com.messdiener.cms.v3.shared.enums.finance.CostCenter;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.web.request.FormFinanceEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceHelper {

    private final BudgetService budgetService;
    private final TransactionService transactionService;
    private final StorageService storageService;
    private final PersonHelper personHelper;
    private final AuditService auditService;

    public void saveBudgetForm(Person user, CostCenter costCenter, BudgetYear budgetYear, List<FormFinanceEntry> entries) throws SQLException {

        budgetService.deleteByCostCenter(costCenter);
        for (FormFinanceEntry formFinanceEntry : entries) {
            Budget budget = new Budget(UUID.randomUUID(), 0, user.getTenantId(), costCenter, budgetYear, formFinanceEntry.getYear() + "", "", formFinanceEntry.getPlannedIncome(), formFinanceEntry.getPlannedExpenditure());
            budgetService.createBudget(budget);
        }
    }

    public double getBudget(Person user, UUID budgetId) throws SQLException {
        return transactionService.getTransactionsSumByBudget(user.getTenantId(), budgetId);
    }

    public void saveBudgetForm(Person user, List<BudgetYear> budgetYears, List<CostCenter> costCenters, List<Double> einnahmen, List<Double> ausgaben) throws SQLException {
        for (int i = 0; i < budgetYears.size(); i++) {
            BudgetYear budgetYear = budgetYears.get(i);
            CostCenter costCenter = costCenters.get(i);
            double income = einnahmen.get(i);
            double expenditure = ausgaben.get(i);

            Optional<Budget> budget = budgetService.getBudgetsByCostCenterAndYear(user.getTenantId(), costCenter, budgetYear);

            if (budget.isPresent()) {

                budget.get().setPlannedIncome(income);
                budget.get().setPlannedExpenditure(expenditure);
                budgetService.updateBudget(budget.get());
            }else {
                Budget b = new Budget(UUID.randomUUID(), 0, user.getTenantId(), costCenter, budgetYear, costCenter.getLabel(), "", income, expenditure);
                budgetService.createBudget(b);
            }
        }
    }

    public Budget getBudget(Person user, CostCenter costCenter, BudgetYear budgetYear) throws SQLException {
        return budgetService.getBudgetsByCostCenterAndYear(user.getTenantId(), costCenter, budgetYear).orElse(new Budget(UUID.randomUUID(), 0, user.getTenantId(), costCenter, budgetYear, costCenter.getLabel(), "", 0,0));
    }

    public double getSumm(UUID transactionId) throws SQLException {
        return storageService.getSumm(transactionId);
    }

    public void createPdf(Person user, Transaction transaction) throws DocumentException, IOException, SQLException {

        // 1) Transaktionen laden
        FileCreator fileCreator = new FileCreator().setFileName(transaction.getId())
                .setFolder("finance")
                .setReceiver("Kath. Pfarrbüro", "Hintere Straße 1", "76756 Bellheim", "")
                .setSender(user, personHelper.getTenantName(user).orElse(""))
                .setImageUrlKn()
                .setSubject("Abrechnung vom " + CMSDate.current().getGermanDate())
                .setInfoline("", "Abrechnung", personHelper.getName(user.getPrincipal()), "F" + user.getFRank());

        fileCreator.addRow(new CMSRow(List.of(new CMSCell("Datum"), new CMSCell("Beleg-Id"), new CMSCell("Bezeichnung"), new CMSCell("Betrag in EURO"))));

        double summ = getSumm(transaction.getId());

        for (StorageFile file : storageService.loadFiles(transaction.getId())) {
            List<CMSCell> cells = List.of(new CMSCell(file.getDate().getGermanDate()), new CMSCell(file.getTag()+""),new CMSCell(file.getTitle()), new CMSCell(file.getMeta() + "€"));
            fileCreator.addRow(new CMSRow(cells));
        }

        fileCreator.addText("Sehr geehrte Damen und Herren,\n" +
                "\n" +
                "anbei befindet sich die Abrechnung der Gruppe " + personHelper.getTenantName(user).orElse("") + "  über den Betrag von " + summ + " Euro.\n" +
                "\n" +
                "Es wird gebeten, diesen Betrag auf folgendes Konto zu überweisen:\n" +
                "\n" +
                user.getBankDetails() + " \n" +
                "\n" +
                "Die entsprechenden Belege sind im Anhang beigefügt.\n" +
                "\n" +
                "Vielen Dank für die Unterstützung.");

        Optional<File> maybeFile = fileCreator.createPdf();

        File pdfFile = maybeFile.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "File not found"));
        storageService.store(new StorageFile(transaction.getId(), 0, Cache.SYSTEM_USER, transaction.getId(), CMSDate.current(), "Transaction.pdf", CMSDate.current(), 0, FileType.ACCOUNTING, pdfFile.getPath()));

        auditService.createLog(AuditLog.of(MessageType.ATTACHMENT_UPLOADED, ActionCategory.FINANCE, transaction.getId(), Cache.SYSTEM_USER, "Abrechnung wurde hinzugefügt", ""));
    }
}
