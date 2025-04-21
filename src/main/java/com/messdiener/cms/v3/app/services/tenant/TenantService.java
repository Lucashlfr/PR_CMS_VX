package com.messdiener.cms.v3.app.services.tenant;

import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
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
public class TenantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_tenant (id VARCHAR(255), name VARCHAR(255), color VARCHAR(255), initial VARCHAR(1))";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("TenantService initialized and table ensured.");

            if (!databaseService.exists("module_tenant", "id", Cache.SYSTEM_TENANT.toString())) {
                save(new Tenant(Cache.SYSTEM_TENANT, "technischer Mandant", "black", "T"));
                LOGGER.info("Default SYSTEM_TENANT created.");
            }

        } catch (SQLException e) {
            LOGGER.error("Failed to initialize TenantService", e);
        }
    }

    public void save(Tenant tenant) throws SQLException {
        databaseService.delete("module_tenant", "id", tenant.getId().toString());
        String sql = "INSERT INTO module_tenant (id, name, color, initial) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenant.getId().toString());
            preparedStatement.setString(2, tenant.getName());
            preparedStatement.setString(3, tenant.getColor());
            preparedStatement.setString(4, tenant.getInitial());
            preparedStatement.executeUpdate();
            LOGGER.info("Tenant '{}' saved.", tenant.getId());
        }
    }

    public Optional<Tenant> findTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT * FROM module_tenant WHERE id = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getByResult(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Tenant> getTenants() throws SQLException {
        List<Tenant> tenants = new ArrayList<>();
        String sql = "SELECT * FROM module_tenant";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                tenants.add(getByResult(resultSet));
            }
        }
        return tenants;
    }

    public Optional<Tenant> findTenantLike(String name) throws SQLException {
        String sql = "SELECT * FROM module_tenant WHERE name LIKE ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getByResult(resultSet)) : Optional.empty();
            }
        }
    }

    private Tenant getByResult(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String name = resultSet.getString("name");
        String color = resultSet.getString("color");
        String initial = resultSet.getString("initial");
        return new Tenant(id, name, color, initial);
    }
}
