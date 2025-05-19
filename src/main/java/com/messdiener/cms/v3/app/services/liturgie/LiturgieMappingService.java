package com.messdiener.cms.v3.app.services.liturgie;

import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.LiturgieState;
import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class LiturgieMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiturgieMappingService.class);
    private final DatabaseService databaseService;
    private final PropertyResolver propertyResolver;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_liturgie_map (liturgieId VARCHAR(36), personId VARCHAR(36), state VARCHAR(255))")) {
            preparedStatement.executeUpdate();
            LOGGER.info("LiturgieService initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing LiturgieService", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("LiturgieService initialized.");
    }

    public LiturgieState getState(UUID liturgieId, UUID personId) throws SQLException {
        String sql = "SELECT state from module_liturgie_map where liturgieId = ? and personId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, liturgieId.toString());
            preparedStatement.setString(2, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return LiturgieState.valueOf(resultSet.getString("state"));
                }
            }
        }
        return LiturgieState.UNAVAILABLE;
    }

    public Map<UUID, Map<UUID, LiturgieState>> getStatesForLiturgies(List<Liturgie> liturgieList, List<PersonOverviewDTO> persons) throws SQLException {
        if (liturgieList.isEmpty() || persons.isEmpty()) {
            return Collections.emptyMap();
        }

        // IDs extrahieren
        List<UUID> liturgieIds = liturgieList.stream()
                .map(Liturgie::getLiturgieId)
                .toList();
        List<UUID> personIds = persons.stream()
                .map(PersonOverviewDTO::getId)
                .toList();

        // Ergebnis-Map mit Default UNAVAILABLE vorbereiten
        Map<UUID, Map<UUID, LiturgieState>> stateMap = new HashMap<>();
        for (UUID litId : liturgieIds) {
            Map<UUID, LiturgieState> inner = new HashMap<>();
            for (UUID personId : personIds) {
                inner.put(personId, LiturgieState.UNAVAILABLE);
            }
            stateMap.put(litId, inner);
        }

        // Platzhalter für IN-Clause bauen
        String litPlaceholders = String.join(",", Collections.nCopies(liturgieIds.size(), "?"));
        String personPlaceholders = String.join(",", Collections.nCopies(personIds.size(), "?"));

        String sql = "SELECT liturgieId, personId, state "
                + "FROM module_liturgie_map "
                + "WHERE liturgieId IN (" + litPlaceholders + ") "
                + "  AND personId   IN (" + personPlaceholders + ")";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int idx = 1;
            // liturgieIds setzen
            for (UUID litId : liturgieIds) {
                preparedStatement.setString(idx++, litId.toString());
            }
            // personIds setzen
            for (UUID personId : personIds) {
                preparedStatement.setString(idx++, personId.toString());
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    UUID litId = UUID.fromString(rs.getString("liturgieId"));
                    UUID personId = UUID.fromString(rs.getString("personId"));
                    LiturgieState state = LiturgieState.valueOf(rs.getString("state"));
                    stateMap.get(litId).put(personId, state);
                }
            }
        }

        return stateMap;
    }

    public void setState(UUID liturgieId, UUID personId, LiturgieState next) throws SQLException {
        databaseService.delete("module_liturgie_map", "personId", personId.toString());
        String sql = "INSERT INTO module_liturgie_map (liturgieId, personId, state) VALUES (?, ?, ?)";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, liturgieId.toString());
            preparedStatement.setString(2, personId.toString());
            preparedStatement.setString(3, next.toString());
            preparedStatement.executeUpdate();
        }
    }
}

