// cms-feature-workflow/.../domain/entity/WorkflowForm.java
package com.messdiener.cms.workflow.domain.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import com.messdiener.cms.shared.ui.Component;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.form.*;
// ggf. weitere konkrete Forms:
// import com.messdiener.cms.workflow.domain.form.WFPrivacyPolicy; // falls vorhanden

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface WorkflowForm {

    UUID getWorkflowId();
    UUID getModuleId();

    WorkflowFormName getUniqueName();

    String getName();

    String getDescription();

    String getImg();

    // === Neu: sinnvolle Defaults, damit nicht jede Form das zwingend überschreiben muss ===
    default String getFLevel() { return "SELF"; }

    int getNumber();

    CMSState getState();

    CMSDate getCreationDate();
    CMSDate getModificationDate();

    List<Component> getComponents();

    // === Neu: no-op Defaults, können von speziellen Forms überschrieben werden ===
    default void preScript() throws SQLException {}
    default void postScript() throws SQLException {}

    String getResults();

    UUID getCurrentUser();

    UUID getCreator();

    void setResult(String result);

    void setState(CMSState state);

    // === NEU: zentrale Factory, die der Mapper und Services verwenden ===
    static WorkflowForm getWorkflowModule(
            UUID workflowId,
            WorkflowFormName name,
            UUID moduleId,
            CMSState state,
            int number,
            CMSDate creationDate,
            CMSDate modificationDate,
            String results,
            UUID currentUser,
            UUID creator
    ) {
        return switch (name) {
            case DATA -> new WFData(workflowId, moduleId, state, number, creationDate, modificationDate, results, currentUser, creator);
            case SAE  -> new WFSae(workflowId, moduleId, state, number, creationDate, modificationDate, results, currentUser, creator);
            case EMERGENCY -> new WFEmergencyContact(workflowId, moduleId, state, number, creationDate, modificationDate, results, currentUser, creator);
            case PRIVACY_POLICY ->  new WFPrivacyPolicy(workflowId, moduleId, state, number, creationDate, modificationDate, results, currentUser, creator);
            case APPROVAL ->  new WFApproval(workflowId, moduleId, state, number, creationDate, modificationDate, results, currentUser, creator);
            default -> throw new IllegalArgumentException("Unsupported WorkflowFormName: " + name);
        };
    }
}
