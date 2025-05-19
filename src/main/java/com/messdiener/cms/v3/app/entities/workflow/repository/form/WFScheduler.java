package com.messdiener.cms.v3.app.entities.workflow.repository.form;

import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class WFScheduler implements WorkflowModule {

    private UUID moduleId;
    private WorkflowModuleStatus status;
    private int number;
    private CMSDate endDate;
    private String results;
    private UUID owner;

    @Override
    public WorkflowModuleName getUniqueName() {
        return WorkflowModuleName.SCHEDULER;
    }

    @Override
    public String getName() {
        return "Dienstplan";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getImg() {
        return "";
    }

    @Override
    public String getFLevel() {
        return "SELF";
    }

    @Override
    public List<Component> getComponents() {
        return new ArrayList<>();
    }

    @Override
    public void preScript() {
    }

    @Override
    public void postScript() throws SQLException {
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }

}
