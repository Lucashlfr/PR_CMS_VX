package com.messdiener.cms.v3.app.entities.workflow.repository.scripts;


import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.repository.ServiceLocator;
import com.messdiener.cms.v3.app.scripts.WorkflowScripts;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class WFInformation implements WorkflowModule {

    private UUID moduleId;
    private WorkflowModuleStatus status;
    private int number;
    private CMSDate endDate;
    private String results;
    private UUID owner;

    @Override
    public WorkflowModuleName getUniqueName() {
        return WorkflowModuleName.INFO;
    }

    @Override
    public String getName() {
        return "Information";
    }

    @Override
    public String getDescription() {
        return "Deine F1 wird automatisch informiert.";
    }

    @Override
    public String getImg() {
        return "";
    }

    @Override
    public String getFLevel() {
        return "INFORMATION";
    }

    @Override
    public List<Component> getComponents() {
        return List.of();
    }

    @Override
    public void preScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        try {
            scripts.preInformation(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postScript() {

    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }
}
