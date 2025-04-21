package com.messdiener.cms.v3.app.services.finance;

import com.messdiener.cms.v3.app.entities.finance.TransactionEntry;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
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

@Service
@RequiredArgsConstructor
public class FinanceQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceQueryService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        LOGGER.info("FinanceQueryService initialized");
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

        Optional<UUID> planerId;
        try {
            planerId = Optional.of(UUID.fromString(resultSet.getString("planerId")));
        } catch (Exception e) {
            planerId = Optional.empty();
        }

        return new TransactionEntry(id, tenant, creator, type, documentDate, costCenter, description, note, value, exchange, active, planerId);
    }

    public Optional<TransactionEntry> getEntry(UUID entryId) throws SQLException {
        String sql = "SELECT * FROM module_finance WHERE id = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, entryId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getByResult(resultSet)) : Optional.empty();
            }
        }
    }

    public List<TransactionEntry> getEntriesByTenant(UUID tenantId) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();
        String sql = "SELECT * FROM module_finance WHERE tenant = ? AND active ORDER BY documentDate";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    financeEntries.add(getByResult(resultSet));
                }
            }
        }
        return financeEntries;
    }

    public List<TransactionEntry> getEntriesByPerson(UUID personId, UUID tenantId) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();
        String sql = "SELECT * FROM module_finance WHERE creator = ? AND tenant = ? AND active";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            preparedStatement.setString(2, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    financeEntries.add(getByResult(resultSet));
                }
            }
        }
        return financeEntries;
    }

    public List<TransactionEntry> getEntriesByPlan(UUID planerId) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();
        String sql = "SELECT * FROM module_finance WHERE planerId = ? AND active ORDER BY documentDate DESC";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    financeEntries.add(getByResult(resultSet));
                }
            }
        }
        return financeEntries;
    }

    public List<TransactionEntry> getEntriesByTenant(UUID tenantId, String type) throws SQLException {
        List<TransactionEntry> financeEntries = new ArrayList<>();
        String sql = "SELECT * FROM module_finance WHERE type = ? AND tenant = ? AND active";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, type);
            preparedStatement.setString(2, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    financeEntries.add(getByResult(resultSet));
                }
            }
        }
        return financeEntries;
    }

    public double getSumm(UUID tenantId, String cashRegisterType) {
        try {
            List<TransactionEntry> entries = getEntriesByTenant(tenantId, cashRegisterType);
            return entries.stream().mapToDouble(TransactionEntry::getValue).sum();
        } catch (SQLException e) {
            LOGGER.error("Error retrieving entries for tenant '{}'", tenantId, e);
            return 0.0;
        }
    }

    public double getSumm(UUID tenantId, String cashRegisterType, boolean positiveValues) {
        try {
            List<TransactionEntry> entries = getEntriesByTenant(tenantId, cashRegisterType);
            return entries.stream()
                    .mapToDouble(entry -> {
                        double value = entry.getValue();
                        if (positiveValues && value > 0) return value;
                        if (!positiveValues && value < 0) return Math.abs(value);
                        return 0;
                    }).sum();
        } catch (SQLException e) {
            LOGGER.error("Error retrieving filtered sum for tenant '{}'", tenantId, e);
            return 0.0;
        }
    }
}
