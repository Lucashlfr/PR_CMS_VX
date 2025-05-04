package com.messdiener.cms.v3.app.services.finance;


import com.messdiener.cms.v3.app.entities.finance.Budget;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.finance.BudgetYear;
import com.messdiener.cms.v3.shared.enums.finance.CostCenter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_finance_budget (id VARCHAR(36), tag INT AUTO_INCREMENT PRIMARY KEY, tenantId VARCHAR(36), costCenter VARCHAR(50), budgetYear VARCHAR(50), name TEXT, description TEXT, plannedIncome double, plannedExpenditure double)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_finance_budget table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_finance_budget", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("ReceiptCenter initialized.");
    }

    private Budget getBudgetByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        int tag = resultSet.getInt("tag");
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));
        CostCenter costCenter = CostCenter.valueOf(resultSet.getString("costCenter"));
        BudgetYear budgetYear = BudgetYear.valueOf(resultSet.getString("budgetYear"));

        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        double plannedIncome = resultSet.getDouble("plannedIncome");
        double plannedExpenditure = resultSet.getDouble("plannedExpenditure");
        return new Budget(id, tag, tenantId, costCenter, budgetYear, name, description, plannedIncome, plannedExpenditure);
    }

    public void createBudget(Budget budget) throws SQLException {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO module_finance_budget (id, tag, tenantId, costCenter, budgetYear, name, description, plannedIncome, plannedExpenditure) VALUES (?,?,?,?,?,?,?,?,?)")) {
            preparedStatement.setString(1, budget.getId().toString());
            preparedStatement.setInt(2, budget.getTag());
            preparedStatement.setString(3, budget.getTenantId().toString());
            preparedStatement.setString(4, budget.getCostCenter().toString());
            preparedStatement.setString(5, budget.getBudgetYear().toString());
            preparedStatement.setString(6, budget.getName());
            preparedStatement.setString(7, budget.getDescription());
            preparedStatement.setDouble(8, budget.getPlannedIncome());
            preparedStatement.setDouble(9, budget.getPlannedExpenditure());
            preparedStatement.executeUpdate();
        }
    }

    public void updateBudget(Budget budget) throws SQLException {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE module_finance_budget SET tag = ?, tenantId = ?, costCenter = ?, budgetYear = ?, name = ?, description = ?, plannedIncome = ?, plannedExpenditure = ? WHERE id = ?")) {
            preparedStatement.setInt(1, budget.getTag());
            preparedStatement.setString(2, budget.getTenantId().toString());
            preparedStatement.setString(3, budget.getCostCenter().toString());
            preparedStatement.setString(4, budget.getBudgetYear().toString());
            preparedStatement.setString(5, budget.getName());
            preparedStatement.setString(6, budget.getDescription());
            preparedStatement.setDouble(7, budget.getPlannedIncome());
            preparedStatement.setDouble(8, budget.getPlannedExpenditure());
            preparedStatement.setString(9, budget.getId().toString());
            preparedStatement.executeUpdate();
        }
    }


    public List<Budget> getBudgetsByCostCenterId(CostCenter costCenter, UUID tenantId) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        try (Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM module_finance_budget WHERE costCenter = ? and tenantId = ? order by name")) {
            preparedStatement.setString(1, costCenter.toString());
            preparedStatement.setString(2, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(getBudgetByResultSet(resultSet));
                }
            }
        }
        return budgets;
    }

    public List<Budget> getBudgetsByTenantId(UUID tenantId) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        try (Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM module_finance_budget WHERE tenantId = ? order by name")) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(getBudgetByResultSet(resultSet));
                }
            }
        }
        return budgets;
    }

    public void deleteByCostCenter(CostCenter costCenter) {
        databaseService.delete("module_finance_budget", "costCenter", costCenter.toString());
    }

    public double getSumm(UUID tenantId) throws SQLException {
        double sum = 0.0;
        for(Budget budget : getBudgetsByTenantId(tenantId)){
            sum = sum + budget.getPlannedIncome() - budget.getPlannedExpenditure();
        }
        return sum;
    }

    public Optional<Budget> getBudgetsByCostCenterAndYear(UUID tenantId, CostCenter costCenter, BudgetYear budgetYear) throws SQLException {
        String sql = "SELECT * FROM module_finance_budget WHERE tenantId = ? AND budgetYear = ? AND costCenter = ? order by name";
        try (Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, budgetYear.toString());
            preparedStatement.setString(3, costCenter.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getBudgetByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Budget> getBudgetsByBudgetYear(UUID tenantId, BudgetYear budgetYear) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM module_finance_budget WHERE tenantId = ? and budgetYear = ? order by name";
        try (Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, budgetYear.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(getBudgetByResultSet(resultSet));
                }
            }
        }
        return budgets;
    }
}