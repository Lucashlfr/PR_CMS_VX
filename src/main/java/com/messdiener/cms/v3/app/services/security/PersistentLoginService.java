package com.messdiener.cms.v3.app.services.security;

import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class PersistentLoginService implements PersistentTokenRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentLoginService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        // Einmalig sicherstellen (einzeiliges SQL)
        String sql = "CREATE TABLE IF NOT EXISTS persistent_logins (username VARCHAR(64) NOT NULL, series VARCHAR(64) PRIMARY KEY, token VARCHAR(64) NOT NULL, last_used TIMESTAMP NOT NULL)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Failed to ensure table persistent_logins", e);
        }
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        String sql = "INSERT INTO persistent_logins (username, series, token, last_used) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, token.getUsername());
            preparedStatement.setString(2, token.getSeries());
            preparedStatement.setString(3, token.getTokenValue());
            preparedStatement.setTimestamp(4, Timestamp.from(token.getDate().toInstant()));
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("createNewToken failed for user={}", token.getUsername(), e);
        }
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        String sql = "UPDATE persistent_logins SET token = ?, last_used = ? WHERE series = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tokenValue);
            preparedStatement.setTimestamp(2, Timestamp.from(lastUsed.toInstant()));
            preparedStatement.setString(3, series);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("updateToken failed for series={}", series, e);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        String sql = "SELECT username, series, token, last_used FROM persistent_logins WHERE series = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, seriesId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String series = resultSet.getString("series");
                    String token = resultSet.getString("token");
                    Instant lastUsed = resultSet.getTimestamp("last_used").toInstant();
                    return new PersistentRememberMeToken(username, series, token, Date.from(lastUsed));
                }
            }
        } catch (Exception e) {
            LOGGER.error("getTokenForSeries failed for series={}", seriesId, e);
        }
        return null;
    }

    @Override
    public void removeUserTokens(String username) {
        String sql = "DELETE FROM persistent_logins WHERE username = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("removeUserTokens failed for user={}", username, e);
        }
    }
}
