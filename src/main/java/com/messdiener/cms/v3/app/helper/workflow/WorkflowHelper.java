package com.messdiener.cms.v3.app.helper.workflow;


import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.tenant.TenantService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowHelper {

    private final WorkflowService workflowService;
    private final TenantService tenantService;
    private final  PersonService personService;

    public boolean createWorkflow(Workflow workflow) {
        try {
            workflowService.createWorkflow(workflow);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean completeWorkflow(Workflow workflow) {
        try {
            workflow.setWorkflowState(WorkflowAttributes.WorkflowState.COMPLETED);
            workflow.setLastUpdateDate(CMSDate.current());
            workflowService.updateWorkflow(workflow);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Optional<Tenant> getTenant(Workflow workflow) {
        try {
            return tenantService.findTenant(workflow.getTenantId());
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public Optional<Person> getPerson(Workflow workflow) {
        try {
            return personService.getPersonById(workflow.getUserId());
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
}
