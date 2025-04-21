package com.messdiener.cms.v3.app.configuration;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final AppConfiguration appConfiguration;

    @PostConstruct
    public void loadConfig() {
        appConfiguration.loadDatabaseConfiguration();
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://" + DatabaseConfiguration.getHost() + ":" + DatabaseConfiguration.getPort() + "/" + DatabaseConfiguration.getDatabase() +
                "?connectTimeout=5000&socketTimeout=10000&autoReconnect=true");
        ds.setUsername(DatabaseConfiguration.getUser());
        ds.setPassword(DatabaseConfiguration.getPassword());
        ds.setMaximumPoolSize(10);
        ds.setConnectionTimeout(10000);
        return ds;
    }
}
