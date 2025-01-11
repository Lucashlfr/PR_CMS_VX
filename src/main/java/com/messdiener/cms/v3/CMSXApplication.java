package com.messdiener.cms.v3;

import com.messdiener.cms.v3.shared.cache.Cache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class CMSXApplication {
    public static void main(String[] args) throws SQLException {
        new Cache().start();
        SpringApplication.run(CMSXApplication.class, args);

        Cache.getPersonService().updateUsers();
        Cache.getUserService().createUserInSecurity();
    }
}