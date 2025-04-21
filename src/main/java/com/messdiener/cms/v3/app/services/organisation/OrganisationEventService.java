package com.messdiener.cms.v3.app.services.organisation;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
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
public class OrganisationEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationEventService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_organisation_events (id VARCHAR(255), tenantId VARCHAR(255), type VARCHAR(255), startDate LONG, endDate LONG, openEnd BOOLEAN, description VARCHAR(255), info TEXT, metaData TEXT)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("OrganisationEventService initialized and table checked.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize OrganisationEventService", e);
        }
    }

    public OrganisationEvent getByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));
        OrganisationType organisationType = OrganisationType.valueOf(resultSet.getString("type"));
        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        CMSDate endDate = CMSDate.of(resultSet.getLong("endDate"));
        boolean openEnd = resultSet.getBoolean("openEnd");
        String description = resultSet.getString("description");
        String info = resultSet.getString("info");
        String metaData = resultSet.getString("metaData");
        return new OrganisationEvent(id, tenantId, organisationType, startDate, endDate, openEnd, description, info, metaData);
    }

    public List<OrganisationEvent> getEvents(UUID tenantId, OrganisationType type) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events WHERE tenantId = ? AND type = ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, type.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(getByResultSet(resultSet));
                }
            }
        }
        return list;
    }

    public List<OrganisationEvent> getEvents(OrganisationType organisationType) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events WHERE type = ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, organisationType.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(getByResultSet(resultSet));
                }
            }
        }
        return list;
    }

    public void saveEvent(OrganisationEvent event) throws SQLException {
        databaseService.delete("module_organisation_events", "id", event.getId().toString());
        String sql = "INSERT INTO module_organisation_events (id, tenantId, type, startDate, endDate, openEnd, description, info, metaData) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, event.getId().toString());
            preparedStatement.setString(2, event.getTenantId().toString());
            preparedStatement.setString(3, event.getOrganisationType().toString());
            preparedStatement.setLong(4, event.getStartDate().toLong());
            preparedStatement.setLong(5, event.getEndDate().toLong());
            preparedStatement.setBoolean(6, event.isOpenEnd());
            preparedStatement.setString(7, event.getDescription());
            preparedStatement.setString(8, event.getInfo());
            preparedStatement.setString(9, event.getMetaData());
            preparedStatement.executeUpdate();
        }
    }

    public Optional<OrganisationEvent> getEventById(UUID id) throws SQLException {
        String sql = "SELECT * FROM module_organisation_events WHERE id = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public List<OrganisationEvent> getNextEvents(UUID tenantId, OrganisationType organisationType) throws SQLException {
        return getNextEvents(tenantId, organisationType, System.currentTimeMillis());
    }

    public List<OrganisationEvent> getNextEvents(UUID tenantId, OrganisationType organisationType, long timestamp) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events WHERE tenantId = ? AND type = ? AND startDate > ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, organisationType.toString());
            preparedStatement.setLong(3, timestamp);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(getByResultSet(resultSet));
                }
            }
        }
        return list;
    }

    public void deleteEvent(UUID id) {
        databaseService.delete("module_organisation_events", "id", id.toString());
        databaseService.delete("module_organisation_map", "eventId", id.toString());
        LOGGER.info("Deleted organisation event and related map entries for id '{}'", id);
    }

    public List<OrganisationEvent> getEvents(UUID tenantId, OrganisationType organisationType, long t) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        long[] range = DateUtils.getFirstAndLastDayOfMonth(t);
        String sql = "SELECT * FROM module_organisation_events WHERE tenantId = ? AND type = ? AND startDate >= ? AND endDate <= ?";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, organisationType.toString());
            preparedStatement.setLong(3, range[0]);
            preparedStatement.setLong(4, range[1]);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(getByResultSet(resultSet));
                }
            }
        }
        return list;
    }
}
