package com.messdiener.cms.v3.app.services.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.event.PreventionForm;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.ComponentType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PreventionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreventionService.class);
    private final DatabaseService databaseService;
    private final PropertyResolver propertyResolver;

    @PostConstruct
    public void init() {
        String sql = """
        CREATE TABLE IF NOT EXISTS prevention_forms (
            id VARCHAR(36) PRIMARY KEY,
            structural_concerns TEXT,
            toilet_signage TEXT,
            room_visibility TEXT,
            welcome_round TEXT,
            photo_policy TEXT,
            complaint_channels TEXT,
            one_on_one_situations TEXT,
            hierarchical_dependencies TEXT,
            communication_channels TEXT,
            decision_transparency TEXT,
            created_at BIGINT
        )
    """;

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.executeUpdate();
            LOGGER.info("prevention_forms table initialized successfully.");

        } catch (SQLException e) {
            LOGGER.error("Error while initializing prevention_forms table", e);
            throw new RuntimeException(e);
        }
    }


    private PreventionForm getPreventionFormByResultSet(ResultSet resultSet) throws SQLException {
        PreventionForm form = new PreventionForm();

        form.setStructuralConcerns(resultSet.getString("structural_concerns"));
        form.setToiletSignage(resultSet.getString("toilet_signage"));
        form.setRoomVisibility(resultSet.getString("room_visibility"));

        form.setWelcomeRound(resultSet.getString("welcome_round"));
        form.setPhotoPolicy(resultSet.getString("photo_policy"));
        form.setComplaintChannels(parseList(resultSet.getString("complaint_channels")));

        form.setOneOnOneSituations(resultSet.getString("one_on_one_situations"));
        form.setHierarchicalDependencies(resultSet.getString("hierarchical_dependencies"));

        form.setCommunicationChannels(parseList(resultSet.getString("communication_channels")));
        form.setDecisionTransparency(parseList(resultSet.getString("decision_transparency")));

        return form;
    }

    public PreventionForm getPreventionForm(UUID id) throws SQLException {
        String sql = "SELECT * FROM prevention_forms WHERE id = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return getPreventionFormByResultSet(resultSet);
                }
            }
        }
        return new PreventionForm();
    }

    public void savePreventionForm(PreventionForm form) throws SQLException {
        String sql = "INSERT INTO prevention_forms (id, structural_concerns, toilet_signage, room_visibility, " +
                "welcome_round, photo_policy, complaint_channels, one_on_one_situations, hierarchical_dependencies, " +
                "communication_channels, decision_transparency, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        databaseService.delete("prevention_forms", "id", form.getId().toString());
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, form.getId().toString());
            preparedStatement.setString(2, form.getStructuralConcerns());
            preparedStatement.setString(3, form.getToiletSignage());
            preparedStatement.setString(4, form.getRoomVisibility());
            preparedStatement.setString(5, form.getWelcomeRound());
            preparedStatement.setString(6, form.getPhotoPolicy());
            preparedStatement.setString(7, toJson(form.getComplaintChannels()));
            preparedStatement.setString(8, form.getOneOnOneSituations());
            preparedStatement.setString(9, form.getHierarchicalDependencies());
            preparedStatement.setString(10, toJson(form.getCommunicationChannels()));
            preparedStatement.setString(11, toJson(form.getDecisionTransparency()));
            preparedStatement.setLong(12, System.currentTimeMillis());

            preparedStatement.executeUpdate();
        }
    }

    private List<String> parseList(String input) {
        if (input == null || input.isBlank()) return new ArrayList<>();
        try {
            return new ObjectMapper().readValue(input, new TypeReference<>() {});
        } catch (Exception e) {
            // Fallback: Trennung nach Komma
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .toList();
        }
    }

    private String toJson(List<String> list) {
        if (list == null) return "[]";
        try {
            return new ObjectMapper().writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }





}
