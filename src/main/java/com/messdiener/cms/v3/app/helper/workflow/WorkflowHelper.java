package com.messdiener.cms.v3.app.helper.workflow;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.workflow.Workflow;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.services.workflow.WorkflowModuleService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
import com.messdiener.cms.v3.shared.enums.workflow.CMSState;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowHelper {

    private final WorkflowService workflowService;
    private final WorkflowModuleService workflowModuleService;

    public void createWorkflow(UUID ownerId, WorkflowType workflowType, WorkflowModuleName... names) throws SQLException {
        Workflow workflow = new Workflow(UUID.randomUUID(), ownerId, workflowType, CMSState.ACTIVE, CMSDate.current(), CMSDate.of(0), 0, "");
        workflowService.saveWorkflow(workflow);

        boolean b = true;
        WorkflowModuleStatus status = WorkflowModuleStatus.OPEN;
        int number = 0;

        for(WorkflowModuleName name : names) {
            WorkflowModule module = WorkflowModule.getWorkflowModule(name, UUID.randomUUID(), status, number, CMSDate.of(-1), "", ownerId);
            workflowModuleService.saveWorkflowModule(workflow.getWorkflowId(), module);

            b = false;
            status = WorkflowModuleStatus.WAITING;
            number++;
        }
    }

    public void nextStep(WorkflowModule module, Workflow workflow) throws SQLException {

        if(workflow.getCurrentNumber() != module.getNumber()){
            return;
        }

        module.setStatus(WorkflowModuleStatus.COMPLETED);
        workflowModuleService.saveWorkflowModule(workflow.getWorkflowId(), module);

        workflow.setCurrentNumber(workflow.getCurrentNumber() + 1);

        if (workflowModuleService.getWorkflowModuleCountByWorkflowId(workflow.getWorkflowId()) <= workflow.getCurrentNumber()) {
            workflow.setCMSState(CMSState.COMPLETED);
        } else {
            WorkflowModule nextModule = workflowModuleService.getModuleByNumber(workflow.getWorkflowId(), workflow.getCurrentNumber()).orElseThrow(() -> new IllegalArgumentException("WorkflowModule not found"));
            nextModule.setStatus(WorkflowModuleStatus.OPEN);
            workflowModuleService.saveWorkflowModule(workflow.getWorkflowId(),nextModule);
        }
        workflowService.saveWorkflow(workflow);
    }

    public String createUrl(Person user, WorkflowModule module, Workflow workflow) throws SQLException {

        if (workflow.getCMSState().equals(CMSState.COMPLETED)) {
            return "/workflow/module/closePage";
        }

        WorkflowModule nextStep = workflowModuleService.getModuleByNumber(workflow.getWorkflowId(), workflow.getCurrentNumber()).orElseThrow(() -> new IllegalArgumentException("WorkflowModule not found"));
        if (!nextStep.getOwner().equals(user.getId())) {
            return "/workflow/module/closePage";
        }

        return "/workflow/module?id=" + nextStep.getModuleId() + "&wf=" + workflow.getWorkflowId();
    }

    public String getFollowUpTasks(String userId, String workflowId) throws SQLException {
        StringBuilder s = new StringBuilder();
        for (WorkflowModule workflowStep : workflowModuleService.getActiveModulesForUserByWorkflow(userId, workflowId)) {
            s.append(workflowStep.getName()).append(", ");
        }
        return s.toString();
    }

    public void createWorkflow(List<Person> users, WorkflowType workflowType) throws SQLException {

        for (Person user : users) {
            switch (workflowType) {
                case SAE -> createWorkflow(user.getId(), WorkflowType.SAE, WorkflowModuleName.SAE);
                case ONBOARDING -> createWorkflow(user.getId(), WorkflowType.ONBOARDING, WorkflowModuleName.DATA, WorkflowModuleName.PRIVACY_POLICY, WorkflowModuleName.EMERGENCY, WorkflowModuleName.APPROVAL);
            }
        }

    }
}
