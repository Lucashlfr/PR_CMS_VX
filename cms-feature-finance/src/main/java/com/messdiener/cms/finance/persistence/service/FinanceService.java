// X:\workspace\PR_CMS\cms-feature-finance\src\main\java\com\messdiener\cms\finance\app\service\FinanceService.java
package com.messdiener.cms.finance.persistence.service;

import com.messdiener.cms.finance.domain.entity.FinanceEntry;
import com.messdiener.cms.finance.persistence.map.FinanceTransactionMapper;
import com.messdiener.cms.finance.persistence.repo.FinanceTransactionRepository;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FinanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceService.class);

    private final FinanceTransactionRepository transactionRepository;
    private final PersonService personService;

    public List<FinanceEntry> getEntriesByTenant(Tenant tenant) {
        return transactionRepository.findByTenantOrderByDateDesc(tenant.toString())
                .stream().map(FinanceTransactionMapper::toDomain).collect(Collectors.toList());
    }

    public void createEntry(FinanceEntry entry) {
        transactionRepository.save(FinanceTransactionMapper.toEntity(entry));
        LOGGER.info("Created finance transaction id={} tenant={}", entry.getId(), entry.getTenant());
    }

    public Map<FinanceEntry, PersonOverviewDTO> getFinancePersonMap(Tenant tenant) {
        Map<FinanceEntry, PersonOverviewDTO> map = new LinkedHashMap<>();
        transactionRepository.findByTenantOrderByDateDesc(tenant.toString()).forEach(e -> {
            FinanceEntry entry = FinanceTransactionMapper.toDomain(e);

            // Neu: Person laden und via Person#toPersonOverviewDTO mappen.
            PersonOverviewDTO dto = personService.getPersonById(entry.getUserId())
                    .map(Person::toPersonOverviewDTO) // vorhandene Domain-Methode
                    .orElse(new PersonOverviewDTO(
                            entry.getUserId(),
                            "", "", entry.getTenant(), "", "",
                            new double[0], "", "", ""
                    ));

            map.put(entry, dto);
        });
        return map;
    }

    public double getSumOfRevenueCashByTenant(Tenant tenant) {
        return transactionRepository.sumRevenueCashByTenant(tenant.toString());
    }

    public double getSumOfExpenseCashByTenant(Tenant tenant) {
        return transactionRepository.sumExpenseCashByTenant(tenant.toString());
    }

    public double getSumOfRevenueAccountByTenant(Tenant tenant) {
        return transactionRepository.sumRevenueAccountByTenant(tenant.toString());
    }

    public double getSumOfExpenseAccountByTenant(Tenant tenant) {
        return transactionRepository.sumExpenseAccountByTenant(tenant.toString());
    }
}
