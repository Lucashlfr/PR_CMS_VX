package com.messdiener.cms.v3.app.services.sql;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.app.configuration.DatabaseConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class DatabaseService {

    private static final Logger LOGGER = Logger.getLogger("Manager.DatabaseService");

    private final AppConfiguration appConfiguration;
    private final DataSource dataSource;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // Verbindung kommt immer frisch vom Pool
    }

    public void testConnection() {
        try (Connection testConn = getConnection()) {
            LOGGER.info("Datenbankverbindung erfolgreich getestet.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Test der Datenbankverbindung", e);
        }
    }

    public boolean exists(String table, String identifierAttribute, String identifier) {
        String query = "SELECT 1 FROM " + table + " WHERE " + identifierAttribute + " = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, identifier);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler bei exists()", e);
            return false;
        }
    }

    public void delete(String table, String identifierAttribute, String identifier) {
        if (!exists(table, identifierAttribute, identifier)) {
            return;
        }

        String query = "DELETE FROM " + table + " WHERE " + identifierAttribute + " = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, identifier);
            stmt.executeUpdate();
            LOGGER.log(Level.FINEST, "Eintrag '{0}' erfolgreich gelöscht.", identifier);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fehler bei delete()", e);
        }
    }
}
