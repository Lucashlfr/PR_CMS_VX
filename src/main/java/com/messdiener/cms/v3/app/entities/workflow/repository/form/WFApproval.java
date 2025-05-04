package com.messdiener.cms.v3.app.entities.workflow.repository.form;

import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.repository.ServiceLocator;
import com.messdiener.cms.v3.app.scripts.WorkflowScripts;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleName;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class WFApproval implements WorkflowModule {

    private UUID moduleId;
    private WorkflowModuleStatus status;
    private int number;
    private CMSDate endDate;
    private String results;
    private UUID owner;

    @Override
    public WorkflowModuleName getUniqueName() {
        return WorkflowModuleName.APPROVAL;
    }

    @Override
    public String getName() {
        return "Freigabe";
    }

    @Override
    public String getDescription() {
        return "Sehr geehrte Kinder und Jugendliche,<br>sehr geehrte Eltern,<br><br>im Rahmen unserer Aktivitäten (Veranstaltungen, Freizeiten usw.) werden Fotos der beteiligten Kinder gemacht. Beispielsweise werden anlässlich einer Freizeit Einzel- und Gruppenaufnahmen angefertigt. Um unsere Tätigkeiten auch nach außen hin zu kommunizieren, sollen gelegentlich auch Fotos in Medien, wie Tageszeitungen und auf unserer Internetseite veröffentlicht werden.<br>Wie bei jeder Internetseite sind die Inhalte jedoch weltweit zu empfangen und zu lesen. Sie können auch kopiert, dupliziert und in anderer Weise verarbeitet werden, ohne dass der Veranstalter als Betreiber der Internetseite die Möglichkeit besitzt, hierauf Einfluss zu nehmen. Daten bleiben auch durch die Speicherung in Suchmaschinen und anderen datensammelnden Internetangeboten, auch nach der Löschung auf unserer Seite, jahrzehntelang erhalten und sind immer wieder abrufbar. Mit diesem Schreiben möchten wir eine grundsätzliche Klärung herbeiführen, ob Sie mit dem Anfertigen und Veröffentlichen von Fotos Ihres Kindes einverstanden sind. Bitte füllen Sie nachfolgende Einwilligung aus; Ihrem Kind entstehen keinerlei Nachteile, wenn Sie mit der Veröffentlichung von Fotos Ihres Kindes insgesamt oder teilweise nicht einverstanden sind.";
    }
    @Override
    public String getImg() {
        return "https://cms.messdiener.com/dist/assets/img/illustrations/Security-rafiki.svg";
    }

    @Override
    public String getFLevel() {
        return "3";
    }

    @Override
    public List<Component> getComponents() {
        List<Component> components = new ArrayList<>();
        return components;
    }

    @Override
    public void preScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.preApproval();
    }

    @Override
    public void postScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.postApproval();
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }

}
