package com.messdiener.cms.v3.app.services.liturgie;

import com.messdiener.cms.v3.app.entities.person.dto.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.shared.enums.tenant.Tenant;
import com.messdiener.cms.v3.utils.other.Pair;
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
    private final PersonService personService;

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

    public void setState(UUID liturgieId,
                         UUID personId,
                         LiturgieState next) throws SQLException {

        final String UPDATE_SQL =
                "UPDATE module_liturgie_map " +
                        "SET    state = ? " +
                        "WHERE  liturgieId = ? AND personId = ?";

        final String INSERT_SQL =
                "INSERT INTO module_liturgie_map (liturgieId, personId, state) " +
                        "VALUES (?,?,?)";

        try (Connection connection = databaseService.getConnection()) {

            // 1) Update-Versuch
            try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, next.toString());
                ps.setString(2, liturgieId.toString());
                ps.setString(3, personId.toString());

                if (ps.executeUpdate() > 0) {
                    return;              // Datensatz existierte – fertig
                }
            }

            // 2) Kein Treffer → Insert
            try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
                ps.setString(1, liturgieId.toString());
                ps.setString(2, personId.toString());
                ps.setString(3, next.toString());
                ps.executeUpdate();
            }
        }
    }

    public List<Pair<PersonOverviewDTO, LiturgieState>> getStateForLiturgy(Tenant tenant, UUID liturgieId) throws SQLException {
        final List<Pair<PersonOverviewDTO, LiturgieState>> liturgyStates = new ArrayList<>();

        // Einzeiliges SQL, LEFT JOIN mit Default-Status und gewünschter Status-Sortierung
        final String sql = "SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant, p.username, p.password,  " +
                "COALESCE(m.state, 'UNAVAILABLE') AS state " +
                "FROM module_person p " +
                "LEFT JOIN module_liturgie_map m ON m.personId = p.person_id AND m.liturgieId = ? " +
                "WHERE p.tenant = ? AND p.active = TRUE " +
                "ORDER BY CASE COALESCE(m.state, 'UNAVAILABLE') " +
                "  WHEN 'DUTY' THEN 1 WHEN 'AVAILABLE' THEN 2 WHEN 'CANCELED' THEN 3 ELSE 4 END, " +
                "p.lastname, p.firstname;";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, liturgieId.toString());
            preparedStatement.setString(2, tenant.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final PersonOverviewDTO person = personService.mapToOverviewDTO(resultSet);
                    final LiturgieState state = LiturgieState.valueOf(resultSet.getString("state"));
                    liturgyStates.add(new Pair<>(person, state));
                }
            }
        }

        return liturgyStates;
    }


    public void unassignWithReason(UUID personId, UUID liturgieId, String reason) throws SQLException {
        setState(liturgieId, personId, LiturgieState.CANCELED);
    }
}

