package com.messdiener.cms.v3.app.services.privacy;

import com.messdiener.cms.v3.app.entities.privacy.PrivacyPolicy;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
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

@Service
@RequiredArgsConstructor
public class PrivacyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_privacy_policy (id VARCHAR(255), date LONG, firstname VARCHAR(255), lastname VARCHAR(255), street VARCHAR(255), houseNumber VARCHAR(255), plz VARCHAR(255), town VARCHAR(255), o1 BOOLEAN, o2 BOOLEAN, o3 BOOLEAN, o4 BOOLEAN, o5 BOOLEAN, o6 BOOLEAN, o7 BOOLEAN, signature LONGBLOB)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PrivacyService initialized and table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PrivacyService", e);
        }
    }

    public void create(PrivacyPolicy privacyPolicy) throws SQLException {
        String sql = "INSERT INTO module_privacy_policy (id, date, firstname, lastname, street, houseNumber, plz, town, o1, o2, o3, o4, o5, o6, o7, signature) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, privacyPolicy.getId().toString());
            preparedStatement.setLong(2, privacyPolicy.getDate().toLong());
            preparedStatement.setString(3, privacyPolicy.getFirstname());
            preparedStatement.setString(4, privacyPolicy.getLastname());
            preparedStatement.setString(5, privacyPolicy.getStreet());
            preparedStatement.setString(6, privacyPolicy.getNumber());
            preparedStatement.setString(7, privacyPolicy.getPlz());
            preparedStatement.setString(8, privacyPolicy.getCity());
            preparedStatement.setBoolean(9, privacyPolicy.isCheck1());
            preparedStatement.setBoolean(10, privacyPolicy.isCheck2());
            preparedStatement.setBoolean(11, privacyPolicy.isCheck3());
            preparedStatement.setBoolean(12, privacyPolicy.isCheck4());
            preparedStatement.setBoolean(13, privacyPolicy.isCheck5());
            preparedStatement.setBoolean(14, privacyPolicy.isCheck6());
            preparedStatement.setBoolean(15, privacyPolicy.isCheck7());
            preparedStatement.setString(16, privacyPolicy.getSignature());
            preparedStatement.executeUpdate();
            LOGGER.info("Privacy policy '{}' created.", privacyPolicy.getId());
        }
    }

    public List<PrivacyPolicy> getAll() throws SQLException {
        List<PrivacyPolicy> privacyPolicies = new ArrayList<>();
        String sql = "SELECT * FROM module_privacy_policy";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                privacyPolicies.add(getPrivacyPolicy(resultSet));
            }
        }
        return privacyPolicies;
    }

    private PrivacyPolicy getPrivacyPolicy(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        CMSDate date = new CMSDate(resultSet.getLong("date"));
        String firstname = resultSet.getString("firstname");
        String lastname = resultSet.getString("lastname");
        String street = resultSet.getString("street");
        String number = resultSet.getString("houseNumber");
        String plz = resultSet.getString("plz");
        String city = resultSet.getString("town");
        boolean check1 = resultSet.getBoolean("o1");
        boolean check2 = resultSet.getBoolean("o2");
        boolean check3 = resultSet.getBoolean("o3");
        boolean check4 = resultSet.getBoolean("o4");
        boolean check5 = resultSet.getBoolean("o5");
        boolean check6 = resultSet.getBoolean("o6");
        boolean check7 = resultSet.getBoolean("o7");
        String signature = resultSet.getString("signature");

        return new PrivacyPolicy(id, date, firstname, lastname, street, number, plz, city, check1, check2, check3, check4, check5, check6, check7, signature);
    }
}
