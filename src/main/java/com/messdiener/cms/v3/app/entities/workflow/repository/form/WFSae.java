package com.messdiener.cms.v3.app.entities.workflow.repository.form;

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
public class WFSae implements WorkflowModule {

    private UUID moduleId;
    private WorkflowModuleStatus status;
    private int number;
    private CMSDate endDate;
    private String results;
    private UUID owner;

    @Override
    public WorkflowModuleName getUniqueName() {
        return WorkflowModuleName.SAE;
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
