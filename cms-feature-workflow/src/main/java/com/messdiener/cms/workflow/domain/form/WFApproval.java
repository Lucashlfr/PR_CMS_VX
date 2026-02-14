package com.messdiener.cms.workflow.domain.form;

import com.messdiener.cms.app.infrastructure.service.ServiceLocator;
import com.messdiener.cms.shared.enums.ComponentType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import com.messdiener.cms.shared.ui.Component;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.app.scripts.WorkflowScripts;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class WFApproval implements WorkflowForm {

    private UUID workflowId;
    private UUID moduleId;
    private CMSState state;
    private int number;
    private CMSDate creationDate;
    private CMSDate modificationDate;
    private String results;
    private UUID currentUser;
    private UUID creator;

    @Override
    public WorkflowFormName getUniqueName() {
        return WorkflowFormName.APPROVAL;
    }

    @Override
    public String getName() {
        return "Freigabe";
    }

    @Override
    public String getDescription() {
        return "Bitte gebe die Daten frei";
    }

    @Override
    public String getImg() {
        return "https://cms.messdiener.com/dist/assets/img/illustrations/Security-rafiki.svg";
    }

    @Override
    public String getFLevel() {
        return "SELF";
    }

    @Override
    public List<Component> getComponents() {
        List<Component> components = new ArrayList<>();

        components.add(Component.of(1, ComponentType.CHECKBOX, "check1", "", "Die Daten werden freigegeben", "true", true));
        return components;
    }

    @Override
    public void preScript() {
        // TODO document why this method is empty
    }

    @Override
    public void postScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.postApproval(this);
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }

}
