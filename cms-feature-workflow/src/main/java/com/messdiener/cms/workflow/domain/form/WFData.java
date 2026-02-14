package com.messdiener.cms.workflow.domain.form;

import com.messdiener.cms.app.infrastructure.service.ServiceLocator;
import com.messdiener.cms.shared.enums.ComponentType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import com.messdiener.cms.shared.ui.Component;
import com.messdiener.cms.utils.other.JsonHelper;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.app.scripts.WorkflowScripts;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Data
public class WFData implements WorkflowForm {

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
        return WorkflowFormName.DATA;
    }

    @Override
    public String getName() {
        return "Datenabgleich";
    }

    @Override
    public String getDescription() {
        return "<p>Liebe Messdienerinnen und Messdiener</p>" +
                "                                <p>In diesem Formular überprüfen wir die aktualität unserer Daten. Solltet ihr z.B. keine Handynummer oder E-Mail-Adresse besitzen, das Feld bitte leer lassen.</p>";
    }

    @Override
    public String getImg() {
        return "/dist/assets/img/illustrations/Security-rafiki.svg";
    }

    @Override
    public List<Component> getComponents() {

        Map<String, String> parameters = JsonHelper.parseJsonToParams(results);


        List<Component> components = new ArrayList<>();

        components.add(new Component(0, ComponentType.TEXT, "firstname", "Vorname", parameters.get("firstname"), "", true, new ArrayList<>()));
        components.add(new Component(1, ComponentType.TEXT, "lastname", "Nachname", parameters.get("lastname"), "", true, new ArrayList<>()));
        components.add(new Component(2, ComponentType.TEXT, "phone", "Telefon-Nr", parameters.get("phone"), "", false, new ArrayList<>()));
        components.add(new Component(3, ComponentType.TEXT, "mobile", "Handynummer", parameters.get("mobile"), "", false, new ArrayList<>()));
        components.add(new Component(4, ComponentType.TEXT, "mail", "E-Mail", parameters.get("mail"), "", false, new ArrayList<>()));
        components.add(new Component(5, ComponentType.TEXT, "street", "Straße", parameters.get("street"), "", true, new ArrayList<>()));
        components.add(new Component(6, ComponentType.TEXT, "number", "Hausnummer", parameters.get("number"), "", true,new ArrayList<>()));
        components.add(new Component(7, ComponentType.TEXT, "plz", "PLZ", parameters.get("plz"), "", true, new ArrayList<>()));
        components.add(new Component(8, ComponentType.TEXT, "city", "Ort", parameters.get("city"), "", true, new ArrayList<>()));
        components.add(new Component(9, ComponentType.DATE, "birthday", "Geburtstag", parameters.get("birthday"), "", true, new ArrayList<>()));
        components.add(new Component(10, ComponentType.TEXTAREA, "comment", "Sonstiges", parameters.get("comment"), "", false, new ArrayList<>()));

        return components;
    }

    @Override
    public String getFLevel() {
        return "SELF";
    }

    @Override
    public void preScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.preData(this);
    }

    @Override
    public void postScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        try {
            scripts.postData(this);
        } catch (SQLException e) {
        }
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }
}
