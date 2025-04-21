package com.messdiener.cms.v3.app.helper.person;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.data.EmergencyContact;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.entities.person.data.statistics.PersonStatistics;
import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.services.organisation.OrganisationMappingService;
import com.messdiener.cms.v3.app.services.person.EmergencyContactService;
import com.messdiener.cms.v3.app.services.person.PersonConnectionService;
import com.messdiener.cms.v3.app.services.person.PersonFileService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.tasks.TaskQueryService;
import com.messdiener.cms.v3.app.services.tenant.TenantService;
import com.messdiener.cms.v3.app.services.user.UserPermissionMappingService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowQueryService;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PersonHelper {

    private final OrganisationMappingService organisationMappingService;
    private final PersonFileService personFileService;
    private final TenantService tenantService;
    private final WorkflowQueryService workflowQueryService;
    private final UserPermissionMappingService permissionMappingService;
    private final PersonConnectionService connectionService;
    private final EmergencyContactService emergencyContactService;
    private final TaskQueryService taskQueryService;
    private final PersonService personService;

    public List<UUID> getOrganisationEventIds(Person person, OrganisationType type) throws SQLException {
        return new ArrayList<>();
    }

    public List<String> listFiles(Person person) {
       return new ArrayList<>();
    }

    public Optional<String> getTenantName(Person person) {
        try {
            return tenantService.findTenant(person.getTenantId()).map(t -> t.getName());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Workflow> getOpenWorkflows(Person person) {
        try {
            return workflowQueryService.getWorkflowsByUser(person.getId(), WorkflowAttributes.WorkflowState.PENDING);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean hasPermission(Person person, String permission) {
        try {
            return permissionMappingService.userHasPermission(Person.empty(person.getId()), permission, false);
        } catch (SQLException e) {
            return false;
        }
    }

    public List<PersonConnection> getConnections(Person person) {
        try {
            return connectionService.getConnectionsByHost(person.getId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<EmergencyContact> getEmergencyContacts(Person person) {
        try {
            return emergencyContactService.getEmergencyContactsByPerson(person.getId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<Task> getTasks(Person person) {
        try {
            return taskQueryService.getOpenTasksByUser(person.getId());
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public Optional<Tenant> getTenant(Person person) {
        try {
            return tenantService.findTenant(person.getTenantId());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    //TODO: FIX
    public PersonStatistics getPersonStatistics(Person person) {
        return new PersonStatistics(person,0,0,0,0,0,0,0);
    }

    public String getImgAddress() {
        return "https://images.pexels.com/photos/1704488/pexels-photo-1704488.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1 ";
    }

    public String getPersonSubName(PersonConnection connection) throws SQLException {
        Optional<Person> sub = personService.getPersonById(connection.getSub());
        if(sub.isPresent()) {
            return sub.get().getName();
        }
        return "";
    }
}
