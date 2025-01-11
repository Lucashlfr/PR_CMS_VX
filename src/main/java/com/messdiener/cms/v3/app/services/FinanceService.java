package com.messdiener.cms.v3.app.services;


import com.messdiener.cms.v3.app.entities.finance.TransactionEntry;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.utils.time.CMSDate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FinanceService {

    private final DatabaseService databaseService;

    public FinanceService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        try {

            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_finance (id VARCHAR(255), tenant VARCHAR(255), creator VARCHAR(255), type VARCHAR(255), documentDate long, costCenter VARCHAR(255), description VARCHAR(255), value double, note LONGTEXT, active boolean, exchange boolean)").executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("SQL wurde nicht ausgeführt");
        }
    }

    public void deleteFinanceRequest(TransactionEntry financeEntry) {
        databaseService.delete("module_finance", "id", financeEntry.getId().toString());
    }

    public void saveFinanceRequest(TransactionEntry financeEntry) throws SQLException {
        deleteFinanceRequest(financeEntry);

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement(
                "INSERT INTO module_finance (id, tenant, creator, type, documentDate, costCenter, description, value, note, active, exchange) VALUES (?,?,?,?,?,?,?,?,?,?,?)"
        );

        preparedStatement.setString(1, financeEntry.getId().toString());
        preparedStatement.setString(2, financeEntry.getTenant().toString());
        preparedStatement.setString(3, financeEntry.getCreator().toString());
        preparedStatement.setString(4, financeEntry.getType());

        preparedStatement.setLong(5, financeEntry.getDocumentDate().toLong());

        preparedStatement.setString(6, financeEntry.getCostCenter());
        preparedStatement.setString(7, financeEntry.getDescription());
        preparedStatement.setDouble(8, financeEntry.getValue());
        preparedStatement.setString(9, financeEntry.getNote());

        preparedStatement.setBoolean(10, financeEntry.isActive());
        preparedStatement.setBoolean(11, financeEntry.isExchange());

        preparedStatement.executeUpdate();

    }

    public TransactionEntry getByResult(ResultSet resultSet) throws SQLException {


        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID tenant = UUID.fromString(resultSet.getString("tenant"));
        UUID creator = UUID.fromString(resultSet.getString("creator"));
        String type = resultSet.getString("type");

        CMSDate documentDate = CMSDate.of(resultSet.getLong("documentDate"));

        String costCenter = resultSet.getString("costCenter");
        String description = resultSet.getString("description");
        String note = resultSet.getString("note");

        double value = resultSet.getDouble("value");

        boolean exchange = resultSet.getBoolean("exchange");

        boolean active = resultSet.getBoolean("active");

        return new TransactionEntry(id, tenant, creator, type, documentDate, costCenter, description, note, value, exchange, active);
    }

    public Optional<TransactionEntry> getEntry(UUID uuid) throws SQLException {

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_finance WHERE id = ?");
        preparedStatement.setString(1, uuid.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next() ? Optional.of(getByResult(resultSet)) : Optional.empty();
    }

    public List<TransactionEntry> getEntriesByTenant(UUID tenant) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_finance WHERE tenant = ? AND active order by documentDate");
        preparedStatement.setString(1, tenant.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            financeEntries.add(getByResult(resultSet));
        }

        return financeEntries;
    }

    public List<TransactionEntry> getEntriesByPerson(Person person) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_finance WHERE creator = ? AND tenant = ? AND active");
        preparedStatement.setString(1, person.getId().toString());
        preparedStatement.setString(2, person.getTenant().getId().toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            financeEntries.add(getByResult(resultSet));
        }

        return financeEntries;
    }

    public double getSumm(Tenant tenant, String cashRegisterType) {
        double sum = 0.0;

        try {
            // Rufe die Einträge für den angegebenen Tenant und Typ ab
            List<TransactionEntry> entries = getEntriesByTenant(tenant.getId(), cashRegisterType);

            // Iteriere über die Einträge und summiere die Werte
            for (TransactionEntry entry : entries) {
                sum += entry.getValue();
            }
        } catch (SQLException e) {
            // Logge die Ausnahme und gib einen Hinweis
            System.err.println("Fehler beim Abrufen der Einträge: " + e.getMessage());
            e.printStackTrace();
        }

        return sum;
    }

    public double getSumm(Tenant tenant, String cashRegisterType, boolean positiveValues) {
        double sum = 0.0;

        try {
            // Rufe die Einträge für den angegebenen Tenant und Typ ab
            List<TransactionEntry> entries = getEntriesByTenant(tenant.getId(), cashRegisterType);

            // Iteriere über die Einträge und summiere die Werte entsprechend der Bedingung
            for (TransactionEntry entry : entries) {
                double value = entry.getValue();
                if (positiveValues && value > 0) {
                    sum += value;
                } else if (!positiveValues && value < 0) {
                    sum += Math.abs(value);
                }
            }
        } catch (SQLException e) {
            // Logge die Ausnahme und gib einen Hinweis
            System.err.println("Fehler beim Abrufen der Einträge: " + e.getMessage());
            e.printStackTrace();
        }

        return sum;
    }


    public List<TransactionEntry> getEntriesByTenant(UUID tenantId, String type) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_finance WHERE type = ? AND tenant = ? AND active");
        preparedStatement.setString(1, type);
        preparedStatement.setString(2, tenantId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            financeEntries.add(getByResult(resultSet));
        }

        return financeEntries;
    }
}
