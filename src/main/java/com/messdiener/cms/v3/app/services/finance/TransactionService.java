package com.messdiener.cms.v3.app.services.finance;


import com.messdiener.cms.v3.app.entities.finance.Transaction;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.shared.enums.finance.TransactionState;
import com.messdiener.cms.v3.shared.enums.finance.TransactionType;
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
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_finance_transactions (id VARCHAR(36), tag INT AUTO_INCREMENT PRIMARY KEY, tenantId VARCHAR(36), userId VARCHAR(36), budgetId VARCHAR(36), date long, transactionState VARCHAR(255), targetUserId VARCHAR(36), title TEXT, description TEXT, transactionType VARCHAR(36))")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_finance_transactions table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_finance_transactions", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("ReceiptCenter initialized.");
    }

    private Transaction getByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        int tag = resultSet.getInt("tag");
        UUID tenantId =  UUID.fromString(resultSet.getString("tenantId"));
        UUID userId = UUID.fromString(resultSet.getString("userId"));
        UUID costCenterId =  UUID.fromString(resultSet.getString("budgetId"));

        CMSDate date = CMSDate.of(resultSet.getLong("date"));

        TransactionState transactionState =  TransactionState.valueOf(resultSet.getString("transactionState"));
        UUID targetUserId = UUID.fromString(resultSet.getString("targetUserId"));

        String title =  resultSet.getString("title");
        String description =   resultSet.getString("description");
        TransactionType transactionType = TransactionType.valueOf(resultSet.getString("transactionType"));
        return new Transaction(id, tag, tenantId, userId, costCenterId, date, transactionState, targetUserId, title, description, transactionType);
    }

    public Optional<Transaction> getById(UUID id) throws SQLException {
        String query = "SELECT * FROM module_finance_transactions WHERE id = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return (resultSet.next()) ? Optional.of(getByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public void save(Transaction transaction) throws SQLException {

        databaseService.delete("module_finance_transactions", "id", transaction.getId().toString());

        String sql = "INSERT INTO module_finance_transactions (id, tag, tenantId, userId, budgetId, date, transactionState, targetUserId, title, description, transactionType) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, transaction.getId().toString());
            preparedStatement.setInt(2, transaction.getTag());
            preparedStatement.setString(3, transaction.getTenantId().toString());
            preparedStatement.setString(4, transaction.getUserId().toString());
            preparedStatement.setString(5, transaction.getBudgetId().toString());
            preparedStatement.setLong(6, transaction.getDate().toLong());
            preparedStatement.setString(7, transaction.getTransactionState().toString());
            preparedStatement.setString(8, transaction.getTargetUserId().toString());
            preparedStatement.setString(9, transaction.getTitle());
            preparedStatement.setString(10, transaction.getDescription());
            preparedStatement.setString(11, transaction.getType().toString());
            preparedStatement.executeUpdate();
        }
    }

    public List<Transaction> getTransactionsByTenantId(UUID tenantId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM module_finance_transactions WHERE tenantId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(getByResultSet(resultSet));
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findAllById(UUID tenantId, List<UUID> selectedTags) throws SQLException {
        // Keine Auswahl → leere Liste zurückgeben
        if (selectedTags == null || selectedTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Transaction> transactions = new ArrayList<>();

        for(UUID tag : selectedTags) {
            transactions.add(getById(tag).orElse(null));
        }
        return transactions;
    }

    public double getIncomeOfTheYear(UUID tenantId) {
        int currentYear = Year.now().getValue();
        ZoneId zone = ZoneId.systemDefault();
        long startOfYear = LocalDate.of(currentYear, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli();
        long startOfNextYear = LocalDate.of(currentYear + 1, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli();
        String sql = "SELECT COALESCE(SUM(amount),0) AS sumIncome FROM module_finance_transactions WHERE amount>0 AND date>=? AND date<? AND tenantId=?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, startOfYear);
            ps.setLong(2, startOfNextYear);
            ps.setString(3, tenantId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("sumIncome");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Fehler beim Ermitteln des Einkommens fürs Jahr {} für Tenant {}", currentYear, tenantId, e);
        }
        return 0.0;
    }

    public double getExpenditureOfTheYear(UUID tenantId) {
        int currentYear = Year.now().getValue();
        ZoneId zone = ZoneId.systemDefault();
        long startOfYear = LocalDate.of(currentYear, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli();
        long startOfNextYear = LocalDate.of(currentYear + 1, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli();
        String sql = "SELECT COALESCE(SUM(s.meta),0) AS sumExp FROM module_storage s, module_finance_transactions t WHERE s.meta<0 AND s.date>=? AND s.date<? AND s.target = t.id and t.tenantId = ?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, startOfYear);
            ps.setLong(2, startOfNextYear);
            ps.setString(3, tenantId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("sumExp");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Fehler beim Ermitteln der Ausgaben fürs Jahr {} für Tenant {}", currentYear, tenantId, e);
        }
        return 0.0;
    }

    public double getTransactionsSum(UUID tenantId, TransactionType transactionType) throws SQLException {
        String sql = "SELECT COALESCE(SUM(s.meta),0) AS sumAcc FROM module_finance_transactions t, module_storage s WHERE t.transactionType=? AND t.tenantId=? and t.id = s.target";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, transactionType.toString());
            preparedStatement.setString(2, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("sumAcc");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Fehler beim Ermitteln der Summe für transactionType ACCOUNT für Tenant {}", tenantId, e);
        }
        return 0.0;
    }

    public double getTransactionsSum(UUID tenantId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(s.meta),0) AS sumAcc FROM module_finance_transactions t, module_storage s WHERE tenantId=? and t.id = s.target";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("sumAcc");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Fehler beim Ermitteln der Summe für transactionType ACCOUNT für Tenant {}", tenantId, e);
        }
        return 0.0;
    }

    public double getTransactionsSumByBudget(UUID tenantId, UUID budgetId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(s.meta),0) AS sumAcc FROM module_finance_transactions t, module_storage s WHERE  tenantId=? and budgetId = ? and t.id = s.target";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, budgetId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("sumAcc");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Fehler beim Ermitteln der Summe für budget für Tenant {}", tenantId, e);
        }
        return 0.0;
    }

}