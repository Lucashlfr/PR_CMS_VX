package com.messdiener.cms.v3.app.services.liturgie;

import com.messdiener.cms.v3.app.entities.worship.LiturgieRequest;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.tenant.Tenant;
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
public class LiturgieRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiturgieRequestService.class);
    private final DatabaseService databaseService;
    private final LiturgieMappingService liturgieMappingService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_liturgie_request (requestId VARCHAR(36), number INT AUTO_INCREMENT PRIMARY KEY, tenant VARCHAR(4), creatorId VARCHAR(36), name TEXT, startDate long, endDate long, deadline long, active boolean)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_liturgie_request initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_liturgie_request", e);
            throw new RuntimeException(e);
        }

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_liturgie_request_map (requestId VARCHAR(36), personId VARCHAR(36), date long)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_liturgie_request_map initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing module_liturgie_request_map", e);
            throw new RuntimeException(e);
        }

        LOGGER.info("LiturgieRequestService initialized.");
    }

    private LiturgieRequest getLiturgieRequestByResult(ResultSet resultSet) throws SQLException {
        UUID requestId = UUID.fromString(resultSet.getString("requestId"));
        Tenant tenant = Tenant.valueOf(resultSet.getString("tenant"));
        UUID creatorId =  UUID.fromString(resultSet.getString("creatorId"));

        int number =  resultSet.getInt("number");

        String name =   resultSet.getString("name");

        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        CMSDate endDate = CMSDate.of(resultSet.getLong("endDate"));

        CMSDate deadline =  CMSDate.of(resultSet.getLong("deadline"));
        boolean active = resultSet.getBoolean("active");

        return new LiturgieRequest(requestId, tenant, creatorId, number, name, startDate, endDate, deadline, active);
    }

    public List<LiturgieRequest> getRequestsByTenant(Tenant tenant) throws SQLException {
        List<LiturgieRequest> liturgieRequests = new ArrayList<>();
        String sql = "SELECT * FROM module_liturgie_request WHERE tenant = ? ORDER BY requestId DESC";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1,tenant.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    liturgieRequests.add(getLiturgieRequestByResult(resultSet));
                }
            }
        }
        return liturgieRequests;
    }

    public void saveRequest(LiturgieRequest liturgieRequest) throws SQLException {
        databaseService.delete("module_liturgie_request", "requestId", liturgieRequest.getRequestId().toString());

        String sql = "INSERT INTO module_liturgie_request (requestId, number, tenant, creatorId, name, startDate, endDate, deadline, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, liturgieRequest.getRequestId().toString());
            preparedStatement.setInt(2, liturgieRequest.getNumber());
            preparedStatement.setString(3, liturgieRequest.getTenant().toString());
            preparedStatement.setString(4, liturgieRequest.getCreatorId().toString());
            preparedStatement.setString(5, liturgieRequest.getName());
            preparedStatement.setLong(6, liturgieRequest.getStartDate().toLong());
            preparedStatement.setLong(7, liturgieRequest.getEndDate().toLong());
            preparedStatement.setLong(8, liturgieRequest.getDeadline().toLong());
            preparedStatement.setBoolean(9, liturgieRequest.isActive());
            preparedStatement.executeUpdate();
        }
    }

    public Optional<LiturgieRequest> currentRequest(Tenant tenant) throws SQLException {
        String sql = "SELECT * FROM module_liturgie_request WHERE tenant = ? and active ORDER BY requestId DESC LIMIT 1";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1,tenant.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next() ? Optional.of(getLiturgieRequestByResult(resultSet)) : Optional.empty();
            }
        }
    }

    public void acceptRequest(UUID personId, UUID requestId) throws SQLException {
        String sql = "INSERT INTO module_liturgie_request_map (requestId, personId, date) VALUES (?, ?, ?)";
        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1,requestId.toString());
            preparedStatement.setString(2,personId.toString());
            preparedStatement.setLong(3,System.currentTimeMillis());
            preparedStatement.executeUpdate();
        }
    }

    public boolean sendRequest(UUID personId, UUID requestId) throws SQLException {
        String sql = "SELECT 1 FROM module_liturgie_request_map WHERE personId = ? AND requestId = ?";
        try(Connection connection = databaseService.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1,personId.toString());
            preparedStatement.setString(2,requestId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next();
            }
        }
    }

    public Map<String, Boolean> getPersonStatusMap(Tenant tenant, UUID requestId) throws SQLException {
        Map<String, Boolean> personStatusMap = new LinkedHashMap<>();

        String personQuery = "SELECT p.person_id, p.firstname, p.lastname FROM module_person p WHERE p.type = 'MESSDIENER' AND p.active AND p.tenant = ? ORDER BY p.lastname";
        String statusQuery = "SELECT 1 FROM module_liturgie_request_map WHERE personId = ? AND requestId = ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement personStatement = connection.prepareStatement(personQuery);
             PreparedStatement statusStatement = connection.prepareStatement(statusQuery)) {

            personStatement.setObject(1, tenant.toString());
            ResultSet personResult = personStatement.executeQuery();

            while (personResult.next()) {
                UUID personId = UUID.fromString(personResult.getString("person_id"));
                String fullName = personResult.getString("firstname") + " " + personResult.getString("lastname");

                statusStatement.setObject(1, personId.toString());
                statusStatement.setObject(2, requestId.toString());

                try (ResultSet statusResult = statusStatement.executeQuery()) {
                    personStatusMap.put(fullName, statusResult.next()); // true wenn Resultat vorhanden
                }
            }
        }

        return personStatusMap;
    }


}
