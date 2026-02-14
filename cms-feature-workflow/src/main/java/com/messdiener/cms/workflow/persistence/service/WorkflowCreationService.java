package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.audit.domain.entity.AuditLog;
import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowCreationService {

    private final WorkflowService workflowService;
    private final FormService formService;
    private final AuditService auditService;

    public void createWorkflow(List<UUID> targetIds, UUID creator, UUID manager, WorkflowType workflowType) {

        for (UUID id : targetIds) {
            switch (workflowType) {
                case ONBOARDING -> createWorkflow(workflowType, id, creator, manager, WorkflowFormName.DATA, WorkflowFormName.PRIVACY_POLICY, WorkflowFormName.EMERGENCY);
                case SAE -> createWorkflow(workflowType, id, creator, manager, WorkflowFormName.SAE);
                default -> {
                    return;
                }
            }
        }

    }


    private void createWorkflow(WorkflowType workflowType, UUID editor, UUID creator, UUID manager, WorkflowFormName... names) {

        UUID workflowId = UUID.randomUUID();
        auditService.createLog(AuditLog.of(MessageType.START, ActionCategory.WORKFLOW, workflowId, Cache.SYSTEM_USER, "Workflow wird erstellt", ""));

        Workflow workflow = Workflow.empty(workflowId, editor, creator, manager, workflowType);
        createFormsToWorkflow(workflow, names);

        workflowService.saveWorkflow(workflow);
        auditService.createLog(AuditLog.of(MessageType.ENDE, ActionCategory.WORKFLOW, workflowId, Cache.SYSTEM_USER, "Workflow wurde erfolgreich erstellt.", ""));
    }

    private void createFormsToWorkflow(Workflow workflow,  WorkflowFormName... names){
        CMSState state = CMSState.OPEN;
        int number = 0;

        for(WorkflowFormName name : names) {
            formService.createForm(workflow.getWorkflowId(), name, state, number, workflow.getEditor(), workflow.getCreator());
            auditService.createLog(AuditLog.of(MessageType.INFO, ActionCategory.WORKFLOW, workflow.getWorkflowId(), Cache.SYSTEM_USER, "WorkflowForm " + name + " erstellt.", ""));

            state = CMSState.WAITING;
            number++;
        }
    }


    public void createApproval(WorkflowForm workflowForm) {
        Workflow workflow = workflowService.getWorkflowById(workflowForm.getWorkflowId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found / Creation Service"));
        formService.createForm(workflow.getWorkflowId(), WorkflowFormName.APPROVAL, CMSState.OPEN, 0, workflow.getManager(), Cache.SYSTEM_USER);
        auditService.createLog(AuditLog.of(MessageType.INFO, ActionCategory.WORKFLOW, workflow.getWorkflowId(), Cache.SYSTEM_USER, "WorkflowForm Freigabe" + " erstellt.", ""));


    }
}
