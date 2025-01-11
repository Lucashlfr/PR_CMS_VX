package com.messdiener.cms.v3.shared.cache;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.app.services.*;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.nio.file.Path;
import java.sql.SQLException;

@Data
public class Cache {

    @Getter
    private static long refresh;

    public static final AppConfiguration APP_CONFIGURATION = new AppConfiguration(Path.of("./cms_vx/config.yml"));
    public static final String APP_NAME = APP_CONFIGURATION.getValue("service.name");

    private static DatabaseService databaseService;

    @Getter
    private static TenantService tenantService;


    @Getter
    private static FinanceService financeService;

    @Getter
    private static PersonService personService;

    @Getter
    private static OrganisationService organisationService;

    @Getter
    private static UserService userService;

    @Getter
    private static WorkflowService taskService;

    @Getter
    private static PermissionService permissionService;

    @Getter
    private static ConfigurationService configurationService;

    @Getter
    private static WorkflowService workflowService;

    @Getter
    private static PrivacyService privacyService;

    @Getter @Setter
    private static InMemoryUserDetailsManager userDetailsManager;

    @Getter @Setter
    public static PasswordEncoder passwordEncoder;

    public static DatabaseService getDatabaseService() {

        if (databaseService == null) databaseService = new DatabaseService();
        if (databaseService.getConnection() == null) databaseService.reconnect();
        return databaseService;

    }

    public Cache() {
    }

    private static void initial(){
        userDetailsManager = new InMemoryUserDetailsManager();
        Cache.databaseService = getDatabaseService();
        Cache.tenantService = new TenantService(databaseService);
        Cache.financeService = new FinanceService(databaseService);
        Cache.taskService = new WorkflowService(databaseService);
        Cache.personService = new PersonService(databaseService);
        Cache.organisationService = new OrganisationService(databaseService);
        Cache.userService = new UserService(databaseService);
        Cache.permissionService = new PermissionService(databaseService);
        Cache.configurationService = new ConfigurationService(databaseService);
        Cache.workflowService = new WorkflowService(databaseService);
        Cache.privacyService = new PrivacyService(databaseService);
    }

    public void start() {
        Cache.initial();
        try {
            getPersonService().matchPersonToUser();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Cache gestartet.");
    }

}
