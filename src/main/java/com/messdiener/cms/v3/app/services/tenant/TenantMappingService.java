package com.messdiener.cms.v3.app.services.tenant;

import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
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
public class TenantMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantMappingService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try {
            createTable("CREATE TABLE IF NOT EXISTS module_tenant (id VARCHAR(255), name VARCHAR(255), color VARCHAR(255), initial VARCHAR(1))");
            createTable("CREATE TABLE IF NOT EXISTS module_tenant_map (id VARCHAR(255), tenant_id VARCHAR(255), messdiener_id VARCHAR(255))");

            if (databaseService.exists("module_tenant", "id", Cache.SYSTEM_TENANT.toString())) {
                map(Cache.SYSTEM_TENANT, Cache.SYSTEM_USER);
                LOGGER.info("Default mapping for system tenant applied.");
            }
            LOGGER.info("TenantMappingService initialized and tables ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize TenantMappingService", e);
        }
    }

    private void createTable(String sql) throws SQLException {
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    public void map(UUID tenant, UUID user) throws SQLException {
        databaseService.delete("module_tenant_map", "messdiener_id", user.toString());

        String sql = "INSERT INTO module_tenant_map (id, tenant_id, messdiener_id) VALUES (?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, tenant.toString());
            preparedStatement.setString(3, user.toString());
            preparedStatement.executeUpdate();
            LOGGER.info("Mapped user '{}' to tenant '{}'", user, tenant);
        }
    }
}
