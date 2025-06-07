package com.messdiener.cms.v3.app.services.finance;

import com.messdiener.cms.v3.app.entities.finance.FinanceEntry;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.finance.TransactionCategory;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class FinanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceService.class);
    private final DatabaseService databaseService;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_finance_transactions (id VARCHAR(36), number INT AUTO_INCREMENT PRIMARY KEY, tenantId VARCHAR(36), billId VARCHAR(36), userID VARCHAR(36), date long, revenueCash double, expenseCash double, revenueAccount double, expenseAccount double, title TEXT, transactionCategory VARCHAR(60), notes TEXT)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_finance_transactions initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_finance_transactions", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("FinanceService initialized.");
    }

    private FinanceEntry getEntryByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));
        UUID billId = UUID.fromString(resultSet.getString("billId"));
        UUID userId = UUID.fromString(resultSet.getString("userID"));

        int number = resultSet.getInt("number");

        CMSDate date = new CMSDate(resultSet.getLong("date"));
        double revenueCash = resultSet.getDouble("revenueCash");
        double expenseCash = resultSet.getDouble("expenseCash");
        double revenueAccount = resultSet.getDouble("revenueAccount");
        double expenseAccount = resultSet.getDouble("expenseAccount");

        String title = resultSet.getString("title");
        TransactionCategory transactionCategory = TransactionCategory.valueOf(resultSet.getString("transactionCategory"));
        String notes = resultSet.getString("notes");
        return new FinanceEntry(id, tenantId, billId, userId, number, date, revenueCash, expenseCash, revenueAccount, expenseAccount, title, transactionCategory, notes);
    }

    public List<FinanceEntry> getEntriesByTenantId(UUID tenantId) throws SQLException {
        String sql = "SELECT * FROM module_finance_transactions WHERE tenantId = ? ORDER BY date DESC";
        List<FinanceEntry> financeEntries = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    FinanceEntry entry = getEntryByResultSet(resultSet);
                    financeEntries.add(entry);
                }
            }
        }
        return financeEntries;
    }

    public void createEntry(FinanceEntry entry) throws SQLException {
        String sql = "INSERT INTO module_finance_transactions (id, number, tenantId, billId, userID, date, revenueCash, expenseCash, revenueAccount, expenseAccount, title, transactionCategory, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, entry.getId().toString());
            preparedStatement.setInt(2, entry.getNumber());
            preparedStatement.setString(3, entry.getTenantId().toString());
            preparedStatement.setString(4, entry.getBillId().toString());
            preparedStatement.setString(5, entry.getUserId().toString());
            preparedStatement.setLong(6, entry.getDate().getDate());
            preparedStatement.setDouble(7, entry.getRevenueCash());
            preparedStatement.setDouble(8, entry.getExpenseCash());
            preparedStatement.setDouble(9, entry.getRevenueAccount());
            preparedStatement.setDouble(10, entry.getExpenseAccount());
            preparedStatement.setString(11, entry.getTitle());
            preparedStatement.setString(12, entry.getTransactionCategory().toString());
            preparedStatement.setString(13, entry.getNotes());
            preparedStatement.executeUpdate();
        }
    }

    public Map<FinanceEntry, PersonOverviewDTO> getFinancePersonMap(UUID tenantId) throws SQLException {
        String sql = "SELECT * FROM module_finance_transactions WHERE tenantId = ? ORDER BY date DESC";
        Map<FinanceEntry, PersonOverviewDTO> map = new HashMap<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    FinanceEntry entry = getEntryByResultSet(resultSet);
                    map.put(entry, personService.getPersonDTO(UUID.fromString(resultSet.getString("userID"))).orElseThrow());
                }
            }
        }
        return map;
    }

    public double getSumOfRevenueCashByTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT SUM(revenueCash) FROM module_finance_transactions WHERE tenantId = ?";
        return summ(tenantId, sql);
    }

    public double getSumOfExpenseCashByTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT SUM(expenseCash) FROM module_finance_transactions WHERE tenantId = ?";
        return summ(tenantId, sql);
    }

    public double getSumOfRevenueAccountByTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT SUM(revenueAccount) FROM module_finance_transactions WHERE tenantId = ?";
        return summ(tenantId, sql);
    }

    public double getSumOfExpenseAccountByTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT SUM(expenseAccount) FROM module_finance_transactions WHERE tenantId = ?";
        return summ(tenantId, sql);
    }

    private double summ(UUID tenantId, String sql) throws SQLException {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        }
        return 0.0;
    }


}
