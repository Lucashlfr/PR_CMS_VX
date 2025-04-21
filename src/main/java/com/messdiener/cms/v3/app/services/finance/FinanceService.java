package com.messdiener.cms.v3.app.services.finance;

import com.messdiener.cms.v3.app.entities.finance.TransactionEntry;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_finance (id VARCHAR(255), tenant VARCHAR(255), creator VARCHAR(255), type VARCHAR(255), documentDate LONG, costCenter VARCHAR(255), description VARCHAR(255), value DOUBLE, note LONGTEXT, active BOOLEAN, exchange BOOLEAN, planerId VARCHAR(255))";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("Finance table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize finance table", e);
        }
    }

    public void deleteFinanceRequest(UUID entryId) {
        databaseService.delete("module_finance", "id", entryId.toString());
        LOGGER.info("Finance entry with id '{}' deleted.", entryId);
    }

    public void saveFinanceRequest(TransactionEntry financeEntry) throws SQLException {
        deleteFinanceRequest(financeEntry.getId());

        String sql = "INSERT INTO module_finance (id, tenant, creator, type, documentDate, costCenter, description, value, note, active, exchange, planerId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

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
            preparedStatement.setString(12, financeEntry.getPlanerId().map(Object::toString).orElse(""));

            preparedStatement.executeUpdate();
            LOGGER.info("Finance entry with id '{}' saved.", financeEntry.getId());
        }
    }
}
