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
public class WFSae implements WorkflowForm {

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
        return WorkflowFormName.SAE;
    }

    @Override
    public String getName() {
        return "Selbstauskunftserkläreung (SAE)";
    }

    @Override
    public String getDescription() {
        return "Gemäß §3 Absatz 1.2. der \"Rahmenordnung zur Prävention gegen sexualisierte Gewalt an Minderjährigen und schutz- oder hilfsbedürftigen Erwachsenen im Bistum Speyer\" versichere ich hiermit, dass ich nicht wegen einer Straftat im Zusammenhang mit sexualisierter Gewalt (gem. §72a SGB VIII genannte Straftaten) rechtskräftig verurteilt worden bin und auch insoweit keine Ermittlungsverfahren gegen mich eingeleitet worden ist.<br><br>Für den Fall, dass diesbezüglich ein Ermittlungsverfahren gegen mich eingeleitet wird, verpflichte ich mich, dies meinem Dienstvorgesetzten bzw. der Person, die mich zu meiner Tätigkeit beauftragt hat, umgehend mitzuteilen.";
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

        components.add(Component.of(8, ComponentType.SIGNATURE, "signature", "", "", true));
        return components;
    }

    @Override
    public void preScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.preSAE(this);
    }

    @Override
    public void postScript() throws SQLException {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.postSae(this);
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }

}
