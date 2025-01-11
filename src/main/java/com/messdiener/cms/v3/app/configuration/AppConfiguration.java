package com.messdiener.cms.v3.app.configuration;

import com.messdiener.cms.v3.shared.cache.Cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppConfiguration {

    private static final Logger LOGGER = Logger.getLogger("Manager.Config");
    private File configFile;
    private Properties properties;

    public AppConfiguration(Path path) {

        LOGGER.setLevel(Level.ALL);
        LOGGER.info("Datei wird ausgelesen.");


        try {

            if(!path.getParent().toFile().exists()){
                path.getParent().toFile().mkdir();
            }

            boolean create = false;
            configFile = new File(path.toString());
            if(!configFile.exists()){
                if(configFile.createNewFile()){
                    // do something
                }
                create = true;
            }

            if(create){
                this.properties = new Properties();
                properties.setProperty("database.host", "X");
                properties.setProperty("database.port", "x");
                properties.setProperty("database.database", "x");
                properties.setProperty("database.username", "x");
                properties.setProperty("database.password", "x");
                properties.setProperty("service.name", "x");
                properties.setProperty("service.url", "x");

                createConfigFile(properties);
                LOGGER.info("Configuration file created successfully.");
            }


            // Read the configuration file
            this.properties = readConfigFile();
            LOGGER.info("Properties read from the configuration file:");
            DatabaseConfiguration.setHost(getValue("database.host"));
            DatabaseConfiguration.setPort(getValue("database.port"));
            DatabaseConfiguration.setDatabase(getValue("database.database"));
            DatabaseConfiguration.setUser(getValue("database.username"));
            DatabaseConfiguration.setPassword(getValue("database.password"));

        } catch (Exception e){
            e.printStackTrace();
        }
        LOGGER.info("Datei erfolgreich ausgelesen. Service wird als " + Cache.APP_NAME + " gestartet.");
    }

    private void createConfigFile(Properties properties) throws IOException {
        FileWriter writer = new FileWriter(configFile);

        // Write the properties to the configuration file
        properties.store(writer, "Configuration File");

        writer.close();
    }


    private Properties readConfigFile() throws IOException {
        FileInputStream inputStream = new FileInputStream(configFile);

        Properties readProperties = new Properties();

        // Load the properties from the configuration file
        readProperties.load(inputStream);

        inputStream.close();

        return readProperties;
    }

    public String getValue(String key) {
        return this.properties.getProperty(key);
    }
}
