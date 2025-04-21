package com.messdiener.cms.v3.shared.cache;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.app.services.configuration.ConfigurationService;
import com.messdiener.cms.v3.app.services.finance.FinanceQueryService;
import com.messdiener.cms.v3.app.services.finance.FinanceService;
import com.messdiener.cms.v3.app.services.organisation.OrganisationAnalyticsService;
import com.messdiener.cms.v3.app.services.organisation.OrganisationEventService;
import com.messdiener.cms.v3.app.services.organisation.OrganisationMappingService;
import com.messdiener.cms.v3.app.services.person.*;
import com.messdiener.cms.v3.app.services.planner.*;
import com.messdiener.cms.v3.app.services.privacy.PrivacyService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.app.services.tasks.TaskMessageService;
import com.messdiener.cms.v3.app.services.tasks.TaskQueryService;
import com.messdiener.cms.v3.app.services.tasks.TaskService;
import com.messdiener.cms.v3.app.services.tenant.TenantMappingService;
import com.messdiener.cms.v3.app.services.tenant.TenantService;
import com.messdiener.cms.v3.app.services.user.PermissionService;
import com.messdiener.cms.v3.app.services.user.UserConfigurationService;
import com.messdiener.cms.v3.app.services.user.UserPermissionMappingService;
import com.messdiener.cms.v3.app.services.user.UserService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowLogService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowQueryService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.shared.scheduler.GlobalManager;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Getter
@Service
@RequiredArgsConstructor
public class Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    // Konstante Konfigurationswerte
    public static final UUID SYSTEM_TENANT = UUID.fromString("d4c5b381-8022-43bf-a67e-5b44562bb94f");
    public static final UUID SYSTEM_USER = UUID.fromString("93dacda6-b951-413a-96dc-9a37858abe3e");

    private final AppConfiguration appConfiguration;
    // Dienste (werden von Spring injiziert)
    private final DatabaseService databaseService;
    private final TenantService tenantService;
    private final TenantMappingService tenantMappingService;
    private final FinanceService financeService;
    private final FinanceQueryService financeQueryService;
    private final PersonService personService;
    private final PersonFileService personFileService;
    private final PersonConnectionService personConnectionService;
    private final EmergencyContactService emergencyContactService;
    private final PersonLoginService personLoginService;
    private final OrganisationEventService organisationEventService;
    private final OrganisationMappingService organisationMappingService;
    private final OrganisationAnalyticsService organisationAnalyticsService;
    private final UserService userService;
    private final UserConfigurationService userConfigurationService;
    private final PermissionService permissionService;
    private final UserPermissionMappingService userPermissionMappingService;
    private final ConfigurationService configurationService;
    private final WorkflowService workflowService;
    private final WorkflowLogService workflowLogService;
    private final WorkflowQueryService workflowQueryService;
    private final PrivacyService privacyService;
    private final PlannerMappingService plannerService;
    private final PlannerEventService plannerEventService;
    private final PlannerLogService plannerLogService;
    private final PlannerTextService plannerTextService;
    private final PlannerEditorService plannerEditorService;
    private final PlannerSubEventService plannerSubEventService;
    private final PlannerTaskService plannerTaskService;
    private final TaskService taskService;
    private final TaskQueryService taskQueryService;
    private final TaskMessageService taskMessageService;
    private final GlobalManager globalManager;

    @PostConstruct
    public void init() {
        LOGGER.info("Cache initialized by Spring.");
            LOGGER.info("Post-init setup for person login and normed tasks completed.");
    }
}
