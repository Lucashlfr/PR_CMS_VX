package com.messdiener.cms.v3.app.entities.workflow.repository.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.repository.ServiceLocator;
import com.messdiener.cms.v3.app.scripts.WorkflowScripts;
import com.messdiener.cms.v3.shared.enums.ComponentType;
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
public class WFEmergencyContact implements WorkflowModule {

    private UUID moduleId;
    private WorkflowModuleStatus status;
    private int number;
    private CMSDate endDate;
    private String results;
    private UUID owner;


    @Override
    public WorkflowModuleName getUniqueName() {
        return WorkflowModuleName.EMERGENCY;
    }

    @Override
    public String getName() {
        return "Notfallkontakt";
    }

    @Override
    public String getDescription() {
        return "Liebe Messdienerinnen und Messdiener<br>" + "Hinterlegt bitte in diesem Formular die Kontaktdaten euerer Eltern.";
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

        Component matrix = new Component();
        matrix.setNumber(0);
        matrix.setType(ComponentType.MATRIX);
        matrix.setName("emergencyContacts");
        matrix.setLabel("Notfallkontakte");
        matrix.setValue("");
        matrix.setOptions("");
        matrix.setRequired(true); // falls Pflicht
        matrix.setColumns(List.of(
                Component.of(1, ComponentType.TEXT, "firstname", "Vorname","", true),
                Component.of(2, ComponentType.TEXT, "firstname", "Nachname","", true),
                Component.of(3, ComponentType.TEXT, "mail", "E-Mail","", false),
                Component.of(4, ComponentType.TEXT, "phone", "Telefon","", true)
        ));

        components.add(matrix);

        return components;
    }

    @Override
    public void preScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        try {
            scripts.preEmergency(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        try {
            scripts.postEmergency(this);
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }
}
