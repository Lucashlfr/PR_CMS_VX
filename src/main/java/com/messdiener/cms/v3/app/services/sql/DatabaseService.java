package com.messdiener.cms.v3.app.services.sql;

import com.messdiener.cms.v3.app.configuration.DatabaseConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseService {

    private static final Logger LOGGER = Logger.getLogger("Manager.DatabaseService");
    private static final String CONNECTION_URL = "jdbc:mysql://" + DatabaseConfiguration.getHost() + ":" + DatabaseConfiguration.getPort()
            + "/" + DatabaseConfiguration.getDatabase();
    private Connection connection;
    private long counter  = 0;

    public DatabaseService() {

        LOGGER.setLevel(Level.ALL);



        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(CONNECTION_URL, DatabaseConfiguration.getUser(), DatabaseConfiguration.getPassword());
            counter = System.currentTimeMillis() + 18000000L;
            LOGGER.finest("SQL-Verbindung aufgebaut!");

        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.severe("Fehler beim Aufbauen der SQL Verbindung");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {

        if(counter < System.currentTimeMillis())
            reconnect();

        return connection;
    }

    public void reconnect() {

        counter = System.currentTimeMillis() + 18000000L;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(CONNECTION_URL, DatabaseConfiguration.getUser(), DatabaseConfiguration.getPassword());
            LOGGER.finest("SQL-Verbindung aufgebaut!");
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.severe("Fehler beim Aufbauen der SQL Verbindung");
            e.printStackTrace();
        }
    }

    public boolean exists(String module, String identifierAttribute, String identifier) {
        try {
            return getConnection().prepareStatement("SELECT " + identifierAttribute + " FROM  " + module + " WHERE " + identifierAttribute + "='" + identifier + "'").executeQuery().next();
        } catch (SQLException e) {
            LOGGER.severe("Fehler! ");
            e.printStackTrace();
            return false;
        }
    }

    public void delete(String module, String identifierAttribute, String identifier)  {

        if(!exists(module, identifierAttribute, identifier)){
            return;
        }

        try {
            getConnection().prepareStatement("DELETE FROM "+ module + " WHERE " + identifierAttribute + "='" + identifier + "'").executeUpdate();
            LOGGER.log(Level.FINEST, "Entry {} erfolgreich gelöscht.", identifier);
        } catch (SQLException e) {
            LOGGER.severe("Fehler! ");
            e.printStackTrace();
        }
    }

}
