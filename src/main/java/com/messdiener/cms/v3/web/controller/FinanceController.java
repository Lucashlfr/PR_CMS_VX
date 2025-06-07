package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.finance.FinanceEntry;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.EventParticipationDto;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.entities.worship.LiturgieRequest;
import com.messdiener.cms.v3.app.entities.worship.LiturgieView;
import com.messdiener.cms.v3.app.helper.liturgie.LiturgieHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.finance.FinanceService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieMappingService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieRequestService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.shared.enums.finance.TransactionCategory;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.*;

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
    public String finance(HttpSession session, Model model, @RequestParam("startDate")Optional<String> startDateS,  @RequestParam("endDate")Optional<String> endDateS, @RequestParam("q")Optional<String> q, @RequestParam("id")Optional<String> idS) throws SQLException {
        Person person = securityHelper.addPersonToSession(session).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        model.addAttribute("person", person);
        model.addAttribute("now", CMSDate.current());

        String query = q.orElse("list");
        model.addAttribute("q", query);

        String startDateE = startDateS.orElse("");
        String endDateE = endDateS.orElse("");
        CMSDate startDate = startDateS.isPresent() ? CMSDate.convert(startDateE, DateUtils.DateType.ENGLISH) : CMSDate.of(liturgieHelper.getStartOfCurrentMonthMillis());
        CMSDate endDate = endDateS.isPresent() ? CMSDate.convert(endDateE, DateUtils.DateType.ENGLISH) : CMSDate.of(liturgieHelper.getEndOfCurrentMonthMillis());

        model.addAttribute("persons", personService.getActivePersonsByPermissionDTO(person.getFRank(), person.getTenantId()));
        model.addAttribute("categories", TransactionCategory.values());
        model.addAttribute("bills", List.of(UUID.randomUUID()));

        model.addAttribute("startDate", startDateE);
        model.addAttribute("endDate", endDateE);

        model.addAttribute("transactions", (Map<FinanceEntry, PersonOverviewDTO>) financeService.getFinancePersonMap(person.getTenantId()));

        List<StorageFile> files = storageService.getFiles(person.getId(), FileType.TRANSACTION);
        model.addAttribute("files", files);

        double cash = financeService.getSumOfRevenueCashByTenant(person.getTenantId()) - financeService.getSumOfExpenseCashByTenant(person.getTenantId());
        double account =  financeService.getSumOfRevenueAccountByTenant(person.getTenantId()) -  financeService.getSumOfExpenseAccountByTenant(person.getTenantId());
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
    public RedirectView save(@RequestParam("date")String dateE, @RequestParam("incomeCash")double incomeCash, @RequestParam("expenseCash")double expenseCash,
                             @RequestParam("incomeBank")double incomeBank, @RequestParam("expenseBank")double expenseBank, @RequestParam("title")String title,
                             @RequestParam("category")TransactionCategory category, @RequestParam("receipt")UUID receipt, @RequestParam("personId")UUID personId,
                             @RequestParam("note")String note) throws SQLException {
        Person person = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        FinanceEntry financeEntry = new FinanceEntry(UUID.randomUUID(), person.getTenantId(), receipt, personId, 0, CMSDate.convert(dateE, DateUtils.DateType.ENGLISH), incomeCash, expenseCash, incomeBank, expenseBank, title, category, note);
        financeService.createEntry(financeEntry);

        return new RedirectView("/finance");
    }


}
