package com.messdiener.cms.v3.web.controller.finance;


import com.itextpdf.text.DocumentException;
import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.finance.Transaction;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.table.CMSCell;
import com.messdiener.cms.v3.app.entities.table.CMSRow;
import com.messdiener.cms.v3.app.export.FileCreator;
import com.messdiener.cms.v3.app.helper.finance.FinanceHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.event.EventService;
import com.messdiener.cms.v3.app.services.finance.BudgetService;
import com.messdiener.cms.v3.app.services.finance.TransactionService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.exception.StorageException;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.shared.enums.finance.BudgetYear;
import com.messdiener.cms.v3.shared.enums.finance.CostCenter;
import com.messdiener.cms.v3.shared.enums.finance.TransactionState;
import com.messdiener.cms.v3.shared.enums.finance.TransactionType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class FinanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceController.class);
    private final Cache cache;
    private final SecurityHelper securityHelper;
    private final FinanceHelper financeHelper;
    private final BudgetService budgetService;
    private final PersonService personService;
    private final TransactionService transactionService;
    private final StorageService storageService;
    private final AuditService auditService;
    private final PersonHelper personHelper;
    private final EventService eventService;

    @PostConstruct
    public void init() {
        LOGGER.info("FinanceController initialized.");
    }

    @GetMapping("/finance")
    public String index(HttpSession httpSession, Model model) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);
        model.addAttribute("user", user);
        model.addAttribute("budgets", budgetService.getBudgetsByBudgetYear(user.getTenantId(), Cache.BUDGET_YEAR));
        model.addAttribute("transactions", transactionService.getTransactionsByTenantId(user.getTenantId()));

        model.addAttribute("expenditureOfTheYear", transactionService.getExpenditureOfTheYear(user.getTenantId()));
        model.addAttribute("incomeOfTheYear", transactionService.getIncomeOfTheYear(user.getTenantId()));
        model.addAttribute("volumeAccount", transactionService.getTransactionsSum(user.getTenantId(), TransactionType.ACCOUNT));
        model.addAttribute("volumeCash", transactionService.getTransactionsSum(user.getTenantId(), TransactionType.CASH));
        model.addAttribute("liquid", transactionService.getTransactionsSum(user.getTenantId()));
        model.addAttribute("global", (-1) * budgetService.getSumm(user.getTenantId()) - transactionService.getTransactionsSum(user.getTenantId()));

        model.addAttribute("financeHelper", financeHelper);

        model.addAttribute("audit", auditService.getLogsByConnectId(user.getTenantId()));
        model.addAttribute("personHelper", personHelper);

        return "finance/list/financeOverview";
    }

    @GetMapping("/finance/budget")
    public String create(Model model) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        model.addAttribute("costCenter", CostCenter.values());
        model.addAttribute("budgetYear", BudgetYear.values());
        model.addAttribute("financeHelper", financeHelper);

        return "finance/form/budgetForm";
    }

    @PostMapping("/finance/budget/save")
    public String handleFormSubmit(
            @RequestParam("budgetYear") List<BudgetYear> budgetYears,
            @RequestParam("costCenter") List<CostCenter> costCenters,
            @RequestParam("income") List<Double> income,
            @RequestParam("expenditure") List<Double> expenditure
    ) throws SQLException {
        if (!(budgetYears.size() == costCenters.size() && income.size() == expenditure.size())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        financeHelper.saveBudgetForm(budgetYears, costCenters, income, expenditure);

        return "close-popup";
    }

    @GetMapping("/finance/transaction/create")
    public String createTransaction(HttpSession httpSession, Model model) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        model.addAttribute("users", personService.getActiveMessdienerByTenant(user.getTenantId()));
        model.addAttribute("transactionStates", TransactionState.values());
        model.addAttribute("types", TransactionType.values());

        Transaction transaction = Transaction.empty(user.getTenantId(), user.getId());
        model.addAttribute("owner", personService.getPersonById(transaction.getTargetUserId()).orElseThrow());
        model.addAttribute("budgets", budgetService.getBudgetsByBudgetYear(user.getTenantId(), Cache.BUDGET_YEAR));
        model.addAttribute("transaction", transaction);

        return "finance/form/transactionForm";
    }

    @GetMapping("/finance/transaction")
    public String transaction(HttpSession httpSession, Model model, @RequestParam("id") UUID id, @RequestParam("s") int s) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        model.addAttribute("users", personService.getActiveMessdienerByTenant(user.getTenantId()));
        model.addAttribute("transactionStates", TransactionState.values());
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("s", s);

        Transaction transaction = transactionService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        model.addAttribute("owner", personService.getPersonById(transaction.getTargetUserId()).orElseThrow());
        model.addAttribute("budgets", budgetService.getBudgetsByBudgetYear(user.getTenantId(), Cache.BUDGET_YEAR));

        model.addAttribute("audit", auditService.getLogsByConnectId(transaction.getId()));
        model.addAttribute("personHelper", personHelper);
        model.addAttribute("transaction", transaction);

        model.addAttribute("events", eventService.getEventsByTenantId(user.getTenantId()));
        model.addAttribute("financeHelper", financeHelper);

        model.addAttribute("bills", storageService.loadFiles(transaction.getId()));

        return "finance/interface/transactionInterface";
    }

    @PostMapping(value = "/finance/transaction/save")
    public RedirectView saveTransaction(
            @RequestParam("title") String title,
            @RequestParam("date") String dateE,
            @RequestParam("budget") UUID budget,
            @RequestParam("type") String type,
            RedirectAttributes redirectAttributes) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Transaction transaction = Transaction.empty(user.getTenantId(), user.getId());
        transaction.setTitle(title);
        transaction.setDate(CMSDate.convert(dateE, DateUtils.DateType.ENGLISH));
        transaction.setBudgetId(budget);
        transaction.setType(TransactionType.valueOf(type));
        transaction.setTargetUserId(user.getId());


        redirectAttributes.addFlashAttribute("success", "Transaktion erfolgreich gespeichert.");

        auditService.createLog(AuditLog.of(MessageType.START, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde von " + personHelper.getName(user.getId()) + " erstellt", ""));
        auditService.createLog(AuditLog.of(MessageType.STEP_COMPLETED, ActionCategory.FINANCE, transaction.getId(), Cache.SYSTEM_USER, "Der Schritt " + transaction.getTransactionState().getLabel() + " wurde von " + personHelper.getName(Cache.SYSTEM_USER) + " beendet.", ""));
        transaction.setTransactionState(TransactionState.BILL);
        transactionService.save(transaction);

        auditService.createLog(AuditLog.of(MessageType.STEP_COMPLETED, ActionCategory.FINANCE, transaction.getId(), Cache.SYSTEM_USER, "Transaktion " + transaction.getTitle() + " wurde von " + personHelper.getName(Cache.SYSTEM_USER) + " in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));

        return new RedirectView("/finance/transaction?s=1&id=" + transaction.getId());
    }

    @PostMapping("/finance/transaction/target")
    public RedirectView setTarget(
            @RequestParam("id") UUID id,
            @RequestParam("user") UUID user,
            HttpServletRequest request) throws Exception {

        Person person = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Transaction transaction = transactionService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transaction.setTargetUserId(user);
        transactionService.save(transaction);

        auditService.createLog(AuditLog.of(MessageType.INFO, ActionCategory.FINANCE, transaction.getId(), person.getId(), "Transaktion wurde " + personHelper.getName(user) + " von " + person.getName() + " zugewiesen.", ""));

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/";
        }
        return new RedirectView(referer);
    }

    @PostMapping(value = "/finance/transaction/bill", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RedirectView saveTransaction(
            @RequestParam("id") UUID id,
            @RequestParam("title") String title,
            @RequestParam("date") String dateE,
            @RequestParam("amount") double amount,
            @RequestParam("receipt") MultipartFile receiptFile,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Transaction transaction = transactionService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        storageService.store(receiptFile, new StorageFile(UUID.randomUUID(), 0, user.getId(), transaction.getId(), CMSDate.current(), title, CMSDate.convert(dateE, DateUtils.DateType.ENGLISH), amount));
        transactionService.save(transaction);


        auditService.createLog(AuditLog.of(MessageType.ATTACHMENT_UPLOADED, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Beleg " + title + " wurde hinzugefügt", ""));


        redirectAttributes.addFlashAttribute("success", "Transaktion erfolgreich gespeichert.");
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/";
        }
        return new RedirectView(referer);
    }

    @GetMapping("/transaction/check")
    public RedirectView checkTransaction(HttpServletRequest request, @RequestParam("id")UUID id) throws SQLException, DocumentException, IOException {

        Person user = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Transaction transaction = transactionService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if(transaction.getTransactionState() == TransactionState.CREATED){
            transaction.setTransactionState(TransactionState.BILL);
            auditService.createLog(AuditLog.of(MessageType.STEP_STARTED, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));
            transactionService.save(transaction);
        }else if(transaction.getTransactionState() == TransactionState.BILL && financeHelper.getSumm(transaction.getId()) != 0){
            transaction.setTransactionState(TransactionState.ACCOUNTING);
            financeHelper.createPdf(transaction);
            auditService.createLog(AuditLog.of(MessageType.STEP_COMPLETED, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));
            transactionService.save(transaction);
        }else if(transaction.getTransactionState() == TransactionState.ACCOUNTING){
            transaction.setTransactionState(TransactionState.TRANSFERRED);
            auditService.createLog(AuditLog.of(MessageType.STEP_COMPLETED, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));
            transactionService.save(transaction);
        }else if(transaction.getTransactionState() == TransactionState.TRANSFERRED){
            transaction.setTransactionState(TransactionState.REVIEW);
            auditService.createLog(AuditLog.of(MessageType.STEP_COMPLETED, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));
            transactionService.save(transaction);
        }else if(transaction.getTransactionState() == TransactionState.REVIEW){
            transaction.setTransactionState(TransactionState.COMPLETED);
            auditService.createLog(AuditLog.of(MessageType.STEP_COMPLETED, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));
            transactionService.save(transaction);
        }else if(transaction.getTransactionState() == TransactionState.COMPLETED){
            transaction.setTransactionState(TransactionState.ARCHIVED);
            auditService.createLog(AuditLog.of(MessageType.ENDE, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Transaktion " + transaction.getTitle() + " wurde in den Status " + transaction.getTransactionState().getLabel() + " gesetzt.", ""));
            transactionService.save(transaction);
        }

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/";
        }
        return new RedirectView(referer);
    }

    @PostMapping(value = "/finance/transaction/notes")
    public RedirectView saveTransaction(
            @RequestParam("id") UUID id,
            @RequestParam("notes") String notes,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Transaction transaction = transactionService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transaction.setDescription(notes);
        transactionService.save(transaction);

        auditService.createLog(AuditLog.of(MessageType.EDIT, ActionCategory.FINANCE, transaction.getId(), user.getId(), "Notiz wurde hinzugefügt", ""));


        redirectAttributes.addFlashAttribute("success", "Transaktion erfolgreich gespeichert.");
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/";
        }
        return new RedirectView(referer);
    }


}