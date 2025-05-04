package com.messdiener.cms.v3.app.services.audit;

import com.messdiener.cms.v3.app.entities.audit.AuditLog;
import com.messdiener.cms.v3.app.entities.audit.ComplianceCheck;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.ComplianceType;
import com.messdiener.cms.v3.shared.enums.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.MessageType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Service
@RequiredArgsConstructor
public class ComplianceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplianceService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_compliance (id INT AUTO_INCREMENT PRIMARY KEY, complianceType VARCHAR(255), targetPerson VARCHAR(36), date long, active bool)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("ComplianceService table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }
    }

    private ComplianceCheck getCheckByResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("c.id");
        ComplianceType complianceType =  ComplianceType.valueOf(resultSet.getString("c.complianceType"));
        UUID targetPerson =  UUID.fromString(resultSet.getString("c.targetPerson"));
        CMSDate date =  new CMSDate(resultSet.getLong("c.date"));
        boolean active =   resultSet.getBoolean("c.active");
        return new ComplianceCheck(id, complianceType, targetPerson, date, active);
    }

    public void saveComplianceCheck(ComplianceCheck complianceCheck) throws SQLException {
        String sql = "INSERT INTO module_compliance (complianceType, targetPerson, date, active) VALUES (?,?,?,?)";

        if(exists(complianceCheck.getTargetPerson(), complianceCheck.getComplianceType()))return;

        try(Connection connection =databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, complianceCheck.getComplianceType().toString());
            preparedStatement.setString(2, complianceCheck.getTargetPerson().toString());
            preparedStatement.setLong(3, complianceCheck.getDate().toLong());
            preparedStatement.setBoolean(4, complianceCheck.isActive());
            preparedStatement.executeUpdate();
        }
    }

    public List<ComplianceCheck> getCompliance(int fRank, UUID tenantId) throws SQLException {
        List<ComplianceCheck> checks = new ArrayList<>();
        String sql;
        if (fRank >= 3) {
            sql = "SELECT * FROM module_person p, module_compliance c  WHERE p.type = 'MESSDIENER' AND p.active and p.person_id = c.targetPerson AND c.active order by id";
            try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    checks.add(getCheckByResultSet(resultSet));
                }
            }
        } else {
            sql = "SELECT * FROM module_person p, module_compliance c  WHERE p.type = 'MESSDIENER' AND p.active and p.person_id = c.targetPerson and p.person_id = ? AND c.active order by id";
            try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, tenantId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        checks.add(getCheckByResultSet(resultSet));
                    }
                }
            }
        }

        return checks;
    }

    public boolean exists(UUID id, ComplianceType complianceType) {
        String query = "SELECT 1 FROM module_compliance WHERE targetPerson = ? AND complianceType = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, complianceType.toString());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public void update(ComplianceCheck cc) throws SQLException {
       String sql = "UPDATE module_compliance SET active = ? WHERE complianceType = ? AND targetPerson = ?";
       try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, cc.isActive());
            preparedStatement.setString(2, cc.getComplianceType().toString());
            preparedStatement.setString(3, cc.getTargetPerson().toString());
            preparedStatement.executeUpdate();
       }
    }

}