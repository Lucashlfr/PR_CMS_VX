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
public class WFPrivacyPolicy implements WorkflowForm {

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
        return WorkflowFormName.PRIVACY_POLICY;
    }

    @Override
    public String getName() {
        return "Datenschutz";
    }

    @Override
    public String getDescription() {
        return "Sehr geehrte Kinder und Jugendliche,<br>sehr geehrte Eltern,<br><br>im Rahmen unserer Aktivitäten (Veranstaltungen, Freizeiten usw.) werden Fotos der beteiligten Kinder gemacht. Beispielsweise werden anlässlich einer Freizeit Einzel- und Gruppenaufnahmen angefertigt. Um unsere Tätigkeiten auch nach außen hin zu kommunizieren, sollen gelegentlich auch Fotos in Medien, wie Tageszeitungen, im Pfarrbrief, Instagram und auf unserer Internetseite veröffentlicht werden.<br><br>Weiter Informationen zum Datenschutz der Messdiener Gruppen in der Pfarrei Sie <a href=\"https://messdiener-knittelsheim.de/privacy-policy/\">hier</a><br><br>\n" +
                "Die Einwilligung zur Veröffentlichung der Daten erfolgt freiwillig und kann gegenüber der KiGem/Pfarrei je-derzeit, mit Wirkung für die Zukunft, widerrufen werden. Den schriftlichen Widerruf übersenden Sie bitte an das Kath. Pfarramt Hl. Hildegard von Bingen in Bellheim. Bei Druckwerken ist die Einwilligung nicht mehr widerruflich, sobald der Druckauftrag erteilt ist; Gleiches gilt auch für bereits weitergegebene Fotos (auch in digitaler Form). Wird die Einwilligung nicht widerrufen, gilt sie zeitlich unbeschränkt, d.h. auch über die Beendigung der Zugehörigkeit zur Ministrantengruppe hinaus. Bei Veröffentlichung eines Gruppenfotos führt der spätere Widerruf einer einzelnen Person grundsätzlich nicht dazu, dass das Bild entfernt werden muss. Uns/mir wurde erläutert, dass die Erklärung unseres Einverständnisses völlig freiwillig ist. Die Aufnahme unseres Kindes in die Ministrantengruppe bzw. sein Verbleiben in dieser ist von dem Einverständnis nicht abhängig.<br><br>" +
                "<span class=\"fw-bold\">Datenschutzhinweis:</span><br>" +
                "Uns / mir ist bekannt, dass die im Rahmen des Ministrantendienstes empfangenen personenbezogenen Daten (z. B. Adresslisten von anderen Ministranten, Dienstpläne usw.) ausschließlich zur Erfüllung des Ministrantendienstes verwendet werden dürfen. Die empfangenen Daten dürfen keinesfalls Dritten weitergegeben oder zugänglich gemacht werden und sind nach Beendigung des Ministrantendienstes zu vernichten. Als Personensorgeberechtigte(r) leite(n) wir / ich unsere Kinder zum sorgsamen Umgang mit den personenbezogenen Daten an.\n";
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

        components.add(Component.of(1, ComponentType.CHECKBOX, "check1", "", "Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, für Internet-Präsentationen (Webstine, Instagram, etc.) der Pfarrei / Gruppe verwendet", "true", false));
        components.add(Component.of(2, ComponentType.CHECKBOX, "check2", "","Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, für an andere Eltern - auch in der Form digitaler Speichermedien - weitergegeben werden dürfen.", "true", false));
        components.add(Component.of(3, ComponentType.CHECKBOX, "check3", "","Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, in Mitteilungen an die Mitglieder der katholischen Kirche wie z. B. dem Pfarrbrief wiedergegeben werden dürfen.", "true", false));
        components.add(Component.of(4, ComponentType.CHECKBOX, "check4", "","Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind,  an öffentliche Publikationsorgane zum Zwecke der Veröffentlichung weitergegeben werden dürfen. ", "true", false));
        components.add(Component.of(5, ComponentType.CHECKBOX, "check5", "", "Hiermit willigen wir / willige ich ein, der Name, die Telefonnummer und die Emailadresse des Ministranten an die anderen Ministranten und deren Leiter für dienstliche Absprachen weitergegeben werden dürfen.", "true", false));
        components.add(Component.of(6, ComponentType.CHECKBOX, "check6", "", "Hiermit willigen wir / willige ich ein, der Name des Ministranten in Veröffentlichungen der Einrichtung genannt werden darf.", "true", false));
        components.add(Component.of(7, ComponentType.CHECKBOX, "check7", "", "Sofern das Personensorgerecht nur einer Person zusteht: Ich versichere, dass ich alleiniger Personensorgeberechtigter bin.", "true", false));
        components.add(Component.of(8, ComponentType.SIGNATURE, "signature", "", "", true));
        return components;
    }

    @Override
    public void preScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        scripts.prePrivacy();
    }

    @Override
    public void postScript() {
        WorkflowScripts scripts = ServiceLocator.getBean(WorkflowScripts.class);
        try {
            scripts.postPrivacy(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setResult(String result) {
        this.results = result;
    }

}
