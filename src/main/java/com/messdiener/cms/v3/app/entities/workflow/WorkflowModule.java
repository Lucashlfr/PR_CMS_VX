package com.messdiener.cms.v3.app.entities.workflow;

import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.workflow.repository.form.*;
import com.messdiener.cms.v3.app.entities.workflow.repository.scripts.WFInformation;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
import com.messdiener.cms.v3.utils.time.CMSDate;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface WorkflowModule {

    UUID getModuleId();

    WorkflowModuleName getUniqueName();

    String getName();

    String getDescription();

    String getImg();

    String getFLevel();

    int getNumber();

    WorkflowModuleStatus getStatus();

    CMSDate getEndDate();

    List<Component> getComponents();

    void preScript();

    void postScript() throws SQLException;

    String getResults();

    UUID getOwner();

    void setResult(String result);

    void setStatus(WorkflowModuleStatus status);

    static WorkflowModule getWorkflowModule(WorkflowModuleName uniqueName, UUID moduleId, WorkflowModuleStatus status, int number, CMSDate endDate, String results, UUID owner) {
        return switch (uniqueName) {
            case APPROVAL -> new WFApproval(moduleId, status, number, endDate, results, owner);
            case PRIVACY_POLICY -> new WFPrivacyPolicy(moduleId, status, number, endDate, results, owner);
            case DATA -> new WFData(moduleId, status, number, endDate, results, owner);
            case EMERGENCY -> new WFEmergencyContact(moduleId, status, number, endDate, results, owner);
            case INFO ->  new WFInformation(moduleId, status, number, endDate, results, owner);
            case SAE -> new WFSae(moduleId, status, number, endDate, results, owner);
            case SCHEDULER -> new  WFScheduler(moduleId, status, number, endDate, results, owner);
            default -> throw new IllegalArgumentException("Unknown Workflow Module: " + uniqueName);
        };
    }
}
