package com.messdiener.cms.v3.app.services.planner;

import com.messdiener.cms.v3.app.entities.planer.PlanerEvent;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.app.utils.Utils;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.planer.PlanerState;
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

@Service
@RequiredArgsConstructor
public class PlannerEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerEventService.class);
    private final DatabaseService databaseService;
    private final PlannerTaskService plannerTaskService;
    private final PlannerEditorService plannerEditorService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_planner_master (planerId VARCHAR(255), tenantId VARCHAR(255), id INT AUTO_INCREMENT PRIMARY KEY, managerId VARCHAR(255), eventName TEXT, startDate LONG, imgUrl TEXT, state VARCHAR(255))";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PlannerEventService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerEventService", e);
        }
    }

    public void savePlaner(PlanerEvent planerEvent) throws SQLException {
        databaseService.delete("module_planner_master", "planerId", planerEvent.getPlanerId().toString());

        String sql = "INSERT INTO module_planner_master (planerId, tenantId, id, managerId, eventName, startDate, imgUrl, state) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerEvent.getPlanerId().toString());
            preparedStatement.setString(2, planerEvent.getTenantId().toString());
            preparedStatement.setInt(3, planerEvent.getId());
            preparedStatement.setString(4, Utils.optionalId(planerEvent.getManagerId()));
            preparedStatement.setString(5, planerEvent.getEventName());
            preparedStatement.setLong(6, planerEvent.getStartDate().toLong());
            preparedStatement.setString(7, planerEvent.getImgUrl());
            preparedStatement.setString(8, planerEvent.getPlanerState().toString());
            preparedStatement.executeUpdate();
            LOGGER.info("Saved planner event '{}'", planerEvent.getPlanerId());
        }
    }

    public List<PlanerEvent> getEvents(UUID tenantId) throws SQLException {
        List<PlanerEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM module_planner_master WHERE tenantId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(getPlanerEventByResult(resultSet));
                }
            }
        }
        return events;
    }

    public Optional<PlanerEvent> getEvent(UUID eventId) throws SQLException {
        String sql = "SELECT * FROM module_planner_master WHERE planerId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, eventId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getPlanerEventByResult(resultSet)) : Optional.empty();
            }
        }
    }

    private PlanerEvent getPlanerEventByResult(ResultSet resultSet) throws SQLException {
        UUID planerId = UUID.fromString(resultSet.getString("planerId"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));
        int id = resultSet.getInt("id");
        Optional<UUID> managerId = Utils.getOId(resultSet.getString("managerId"));
        String eventName = resultSet.getString("eventName");
        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        String imgUrl = resultSet.getString("imgUrl");
        PlanerState planerState = PlanerState.valueOf(resultSet.getString("state"));

        return new PlanerEvent(
                planerId,
                tenantId,
                id,
                managerId,
                eventName,
                startDate,
                imgUrl,
                planerState,
                plannerTaskService.getTasks(planerId),
                plannerEditorService.getEditorsByPlaner(planerId)
        );
    }
}
