package com.messdiener.cms.v3.app.services;


import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TenantService {

    private final DatabaseService databaseService;

    public TenantService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        try {
            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_tenant (id VARCHAR(255), name VARCHAR(255), color VARCHAR(255), initial VARCHAR(1))").executeUpdate();
            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_tenant_map (id VARCHAR(255), tenant_id VARCHAR(255), messdiener_id VARCHAR(255))").executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("SQL konnte nicht ausgeführt werden");
        }
    }

    public void save(Tenant tenant) throws SQLException {
        databaseService.delete("module_tenant", "id", tenant.getId().toString());

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement(
                "INSERT INTO module_tenant (id, name, color, initial) VALUES (?,?, ?,?)"
        );

        preparedStatement.setString(1, tenant.getId().toString());
        preparedStatement.setString(2, tenant.getName());
        preparedStatement.setString(3, tenant.getColor());
        preparedStatement.setString(4, tenant.getInitial());

        preparedStatement.executeUpdate();
    }

    public Optional<Tenant> findTenant(UUID tenant) throws SQLException {

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_tenant WHERE id = ?");
        preparedStatement.setString(1, tenant.toString());

        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? Optional.of(getByResult(resultSet)) : Optional.empty();
    }

    private Tenant getByResult(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String name = resultSet.getString("name");
        String color = resultSet.getString("color");
        String inital = resultSet.getString("initial");

        return new Tenant(id, name, color, inital);
    }


    public void map(UUID user, UUID tenant) throws SQLException {

        databaseService.delete("module_tenant_map", "messdiener_id", user.toString());

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement(
                "INSERT INTO module_tenant_map (id, tenant_id, messdiener_id) VALUES (?,?,?)"
        );

        preparedStatement.setString(1, UUID.randomUUID().toString());
        preparedStatement.setString(2, user.toString());
        preparedStatement.setString(3, tenant.toString());

        preparedStatement.executeUpdate();
    }

    public List<Tenant> getTenants() throws SQLException {
        List<Tenant> tenants = new ArrayList<>();
        ResultSet resultSet = databaseService.getConnection().prepareStatement("SELECT * FROM module_tenant").executeQuery();
        while (resultSet.next()){
            tenants.add(getByResult(resultSet));
        }
        return tenants;
    }
}
