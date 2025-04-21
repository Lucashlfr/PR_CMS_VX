package com.messdiener.cms.v3.app.services.planner;

import com.messdiener.cms.v3.app.entities.planer.entities.PlanerText;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlannerTextService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerTextService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_planer_text (textId VARCHAR(255), planerId VARCHAR(255), userId VARCHAR(255), date LONG, tag VARCHAR(255), identifier VARCHAR(255), pText TEXT)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PlannerTextService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerTextService", e);
        }
    }

    public void saveText(UUID planerId, PlanerText planerText) throws SQLException {
        String sql = "INSERT INTO module_planer_text (textId, planerId, userId, date, tag, identifier, pText) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerText.getTextId().toString());
            preparedStatement.setString(2, planerId.toString());
            preparedStatement.setString(3, planerText.getUserId().toString());
            preparedStatement.setLong(4, planerText.getCreationDate().toLong());
            preparedStatement.setString(5, planerText.getTag().toString());
            preparedStatement.setString(6, planerText.getIdentifier());
            preparedStatement.setString(7, planerText.getText());
            preparedStatement.executeUpdate();
            LOGGER.info("Saved planner text '{}' for planner '{}'", planerText.getTextId(), planerId);
        }
    }

    public PlanerText getPlanerText(UUID planerId, UUID userId, PlanerTag tag, String identifier) throws SQLException {
        String sql = "SELECT * FROM module_planer_text WHERE planerId = ? AND tag = ? AND identifier = ? ORDER BY date DESC";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            preparedStatement.setString(2, tag.toString());
            preparedStatement.setString(3, identifier);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? mapToPlanerText(resultSet) : new PlanerText(UUID.randomUUID(), userId, CMSDate.current(), tag, identifier, "");
            }
        }
    }

    private PlanerText mapToPlanerText(ResultSet resultSet) throws SQLException {
        UUID textId = UUID.fromString(resultSet.getString("textId"));
        UUID userId = UUID.fromString(resultSet.getString("userId"));
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        PlanerTag tag = PlanerTag.valueOf(resultSet.getString("tag"));
        String identifier = resultSet.getString("identifier");
        String text = resultSet.getString("pText");
        return new PlanerText(textId, userId, date, tag, identifier, text);
    }
}
