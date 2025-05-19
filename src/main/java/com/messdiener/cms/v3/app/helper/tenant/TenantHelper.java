package com.messdiener.cms.v3.app.helper.tenant;

import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantHelper {

    private final TenantService tenantService;


    public Optional<String> getTenantName(Tenant tenant) {
        try {
            return tenantService.findTenant(tenant.getId()).map(Tenant::getName);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Tenant getTenant(UUID id) throws SQLException {
        return tenantService.findTenant(id).orElse(new Tenant(UUID.randomUUID(), "NULL", "NULL", "NULL"));
    }

}
