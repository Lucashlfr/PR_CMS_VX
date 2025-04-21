package com.messdiener.cms.v3.app.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class AppConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);
    private File configFile;
    private Properties properties;

    private static final String DEFAULT_CONFIG_PATH = "config/app.properties"; // falls du das standardisieren willst

    @PostConstruct
    public void init() {
        Path path = Path.of(DEFAULT_CONFIG_PATH);
        LOGGER.info("Initializing AppConfiguration from file: {}", path);

        try {
            prepareConfigFile(path);
            this.properties = initProperties();
            loadDatabaseConfiguration();
            LOGGER.info("Configuration file successfully loaded. Service started.");
        } catch (IOException e) {
            LOGGER.error("Error reading configuration file: {}", e.getMessage(), e);
        }
    }

    private void prepareConfigFile(Path path) throws IOException {
        File directory = path.getParent().toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.warn("Could not create configuration directory.");
        }

        this.configFile = path.toFile();

        if (!configFile.exists() && configFile.createNewFile()) {
            LOGGER.info("Configuration file created: {}", configFile.getAbsolutePath());
            createDefaultProperties();
        }
    }

    private void createDefaultProperties() throws IOException {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("database.host", "X");
        defaultProps.setProperty("database.port", "x");
        defaultProps.setProperty("database.database", "x");
        defaultProps.setProperty("database.username", "x");
        defaultProps.setProperty("database.password", "x");
        defaultProps.setProperty("service.name", "x");
        defaultProps.setProperty("service.url", "x");

        try (FileWriter writer = new FileWriter(configFile)) {
            defaultProps.store(writer, "Configuration File");
        }

        LOGGER.info("Default configuration properties written.");
    }

    private Properties initProperties() throws IOException {
        Properties loadedProps = new Properties();
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            loadedProps.load(inputStream);
        }
        return loadedProps;
    }

    public void loadDatabaseConfiguration() {
        DatabaseConfiguration.setHost(getValue("database.host"));
        DatabaseConfiguration.setPort(getValue("database.port"));
        DatabaseConfiguration.setDatabase(getValue("database.database"));
        DatabaseConfiguration.setUser(getValue("database.username"));
        DatabaseConfiguration.setPassword(getValue("database.password"));

        LOGGER.debug("Database configuration loaded from properties.");
    }

    public String getValue(String key) {
        if (this.properties == null) {
            try {
                prepareConfigFile(Path.of(DEFAULT_CONFIG_PATH));
                this.properties = initProperties();
            } catch (IOException e) {
                LOGGER.error("Error during lazy config init", e);
                return null;
            }
        }

        return this.properties.getProperty(key);
    }

}
