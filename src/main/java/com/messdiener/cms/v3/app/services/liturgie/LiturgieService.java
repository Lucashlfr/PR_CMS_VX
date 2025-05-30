package com.messdiener.cms.v3.app.services.liturgie;

import com.messdiener.cms.v3.app.entities.worship.EventParticipationDto;
import com.messdiener.cms.v3.app.entities.worship.HeatmapDto;
import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.services.finance.BudgetService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class LiturgieService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiturgieService.class);
    private final DatabaseService databaseService;
    private final LiturgieMappingService liturgieMappingService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_liturgie (liturgieId VARCHAR(36), number INT AUTO_INCREMENT PRIMARY KEY, tenantId VARCHAR(36), liturgieType VARCHAR(50), date long, local boolean)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("LiturgieService initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing LiturgieService", e);
            throw new RuntimeException(e);
        }
        LOGGER.info("LiturgieService initialized.");
    }

    private Liturgie getLiturgieByResultSet(ResultSet resultSet) throws SQLException {
        UUID liturgieId = UUID.fromString(resultSet.getString("liturgieId"));
        int number = resultSet.getInt("number");
        UUID tenantId =  UUID.fromString(resultSet.getString("tenantId"));

        LiturgieType liturgieType =  LiturgieType.valueOf(resultSet.getString("liturgieType"));
        CMSDate date = CMSDate.of(resultSet.getLong("date"));
        boolean local =  resultSet.getBoolean("local");
        return new Liturgie(liturgieId, number, tenantId, liturgieType, date, local);
    }

    public List<Liturgie> getLiturgies(UUID tenantId, long startDate, long endDate) throws SQLException {
        List<Liturgie> liturgies = new ArrayList<>();
        String sql ="SELECT * FROM module_liturgie WHERE tenantId=? AND date>=? AND date<=?";

        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setLong(2, startDate);
            preparedStatement.setLong(3, endDate);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    liturgies.add(getLiturgieByResultSet(resultSet));
                }
            }

        }
        return liturgies;
    }
}
