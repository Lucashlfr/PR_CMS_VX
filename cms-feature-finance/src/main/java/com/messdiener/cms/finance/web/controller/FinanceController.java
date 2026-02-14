package com.messdiener.cms.finance.web.controller;

import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.finance.persistence.service.FinanceService;
import com.messdiener.cms.finance.domain.entity.FinanceEntry;
import com.messdiener.cms.liturgy.app.helper.LiturgieHelper;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.shared.enums.document.FileType;
import com.messdiener.cms.shared.enums.finance.TransactionCategory;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.utils.time.DateUtils;
import com.messdiener.cms.files.domain.entity.StorageFile;
import com.messdiener.cms.files.persistence.service.StorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class FinanceController {

    private final SecurityHelper securityHelper;
    private final LiturgieHelper liturgieHelper;
    private final FinanceService financeService;
    private final PersonService personService;
    private final StorageService storageService;
    private final PersonHelper personHelper;

    @GetMapping("/finance")
    public String finance(
            HttpSession session,
            Model model,
            @RequestParam("startDate") Optional<String> startDateS,
            @RequestParam("endDate") Optional<String> endDateS,
            @RequestParam("q") Optional<String> q,
            @RequestParam("id") Optional<String> idS
    ) throws SQLException {

        // 1) Session-User (View) holen
        PersonSessionView sessionUser = securityHelper.addPersonToSession(session)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        // 2) Domänen-Person nachladen (für tenant, rank usw.)
        Person person = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        model.addAttribute("person", person);
        model.addAttribute("now", CMSDate.current());

        String query = q.orElse("list");
        model.addAttribute("q", query);

        String startDateE = startDateS.orElse("");
        String endDateE = endDateS.orElse("");

        CMSDate startDate = startDateS.isPresent()
                ? CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH)
                : CMSDate.of(liturgieHelper.getStartOfCurrentMonthMillis());

        CMSDate endDate = endDateS.isPresent()
                ? CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH)
                : CMSDate.of(liturgieHelper.getEndOfCurrentMonthMillis());

        model.addAttribute("persons",
                personService.getActivePersonsByPermissionDTO(person.getFRank(), person.getTenant()));
        model.addAttribute("categories", TransactionCategory.values());
        model.addAttribute("bills", List.of(UUID.randomUUID()));

        model.addAttribute("startDate", startDateE);
        model.addAttribute("endDate", endDateE);

        model.addAttribute("transactions",
                (Map<FinanceEntry, PersonOverviewDTO>) financeService.getFinancePersonMap(person.getTenant()));

        List<StorageFile> files = storageService.getFiles(person.getId(), FileType.TRANSACTION);
        model.addAttribute("files", files);

        double cash = financeService.getSumOfRevenueCashByTenant(person.getTenant())
                - financeService.getSumOfExpenseCashByTenant(person.getTenant());
        double account = financeService.getSumOfRevenueAccountByTenant(person.getTenant())
                - financeService.getSumOfExpenseAccountByTenant(person.getTenant());
        double summ = cash + account;

        model.addAttribute("cash", cash);
        model.addAttribute("account", account);
        model.addAttribute("summ", summ);

        if (query.equals("list")) {
            return "finance/list/financeOverview";
        } else if (query.equals("bill")) {
            model.addAttribute("personHelper", personHelper);
            return "finance/list/billOverview";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
    }

    @PostMapping("/transactions/save")
    public RedirectView save(
            @RequestParam("date") String dateE,
            @RequestParam("incomeCash") double incomeCash,
            @RequestParam("expenseCash") double expenseCash,
            @RequestParam("incomeBank") double incomeBank,
            @RequestParam("expenseBank") double expenseBank,
            @RequestParam("title") String title,
            @RequestParam("category") TransactionCategory category,
            @RequestParam("receipt") UUID receipt,
            @RequestParam("personId") UUID personId,
            @RequestParam("note") String note
    ) throws SQLException {

        // Session-View -> Domänen-Person
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person person = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        FinanceEntry financeEntry = new FinanceEntry(
                UUID.randomUUID(),
                person.getTenant(),
                receipt,
                personId,
                0,
                CMSDate.convert(dateE, DateUtils.DateType.ENGLISH),
                incomeCash,
                expenseCash,
                incomeBank,
                expenseBank,
                title,
                category,
                note
        );
        financeService.createEntry(financeEntry);
        return new RedirectView("/finance");
    }
}
