package com.messdiener.cms.v3.app.helper.finance;


import com.messdiener.cms.v3.app.entities.finance.TransactionEntry;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionHelper {

    private final PersonService personService;

    public String getTransactionUrl(UUID id) {
        return "/finance/file?uuid=" + id;
    }

    public String getValueFormatted(TransactionEntry entry) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.GERMANY);
        return df.format(entry.getValue());
    }

    public String getCreatorName(TransactionEntry entry) {
        try {
            Optional<Person> person = personService.getPersonById(entry.getCreator());
            return person.map(Person::getName).orElse("Unbekannt");
        } catch (SQLException e) {
            return "Fehler beim Laden";
        }
    }

    public String getTypeDisplay(String type) {
        return switch (type.toUpperCase()) {
            case "INCOME" -> "Einnahme";
            case "EXPENSE" -> "Ausgabe";
            case "TRANSFER" -> "Umbuchung";
            default -> "Unbekannt";
        };
    }

    public boolean isIncome(TransactionEntry entry) {
        return "INCOME".equalsIgnoreCase(entry.getType());
    }

    public boolean isExpense(TransactionEntry entry) {
        return "EXPENSE".equalsIgnoreCase(entry.getType());
    }

    public String getTransactionLabel(TransactionEntry entry) {
        return isIncome(entry) ? "text-success fw-bold" : isExpense(entry) ? "text-danger fw-bold" : "";
    }
}
