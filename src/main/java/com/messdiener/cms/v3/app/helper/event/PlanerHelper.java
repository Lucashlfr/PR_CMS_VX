package com.messdiener.cms.v3.app.helper.event;

import com.messdiener.cms.v3.app.entities.acticle.Article;
import com.messdiener.cms.v3.app.entities.component.Component;
import com.messdiener.cms.v3.app.entities.document.Document;
import com.messdiener.cms.v3.app.entities.event.PlanerTask;
import com.messdiener.cms.v3.app.services.article.ArticleService;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.event.EventApplicationService;
import com.messdiener.cms.v3.app.services.event.PlannerTaskService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.ArticleType;
import com.messdiener.cms.v3.shared.enums.ComponentType;
import com.messdiener.cms.v3.shared.enums.event.EventType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PlanerHelper {

    private final PlannerTaskService plannerTaskService;
    private final DocumentService documentService;
    private final EventApplicationService eventApplicationService;

    public void createSubTasks(UUID id, EventType eventType) throws SQLException {

        if(eventType == EventType.BIG_ACTION){
            plannerTaskService.updateTask(id, PlanerTask.createPlanerTask("Versicherung", "Hinterlegung und Verwaltung der gegebenenfalls erforderlichen Unterlagen.", "INSURANCE"));

            PlanerTask concept = PlanerTask.createPlanerTask("Hygiene- und Präventionskonzept", "Erstellung eines Hygiene- und Präventionskonzeptes auf der Grundlage einer Risikoanalyse.", "TEXT");
            plannerTaskService.updateTask(id, concept);
            documentService.saveDocument(new Document(concept.getTaskId(), Cache.SYSTEM_USER, id, CMSDate.current(), "Hygiene- und Präventionskonzept", getContent(), "F1"));
        }
    }

    public void createComponents(UUID id) throws SQLException {

        Component cFirstName = Component.of(1, ComponentType.TEXT,  "firstname", "Vorname","",true);
        Component cLastName = Component.of(2, ComponentType.TEXT,  "lastname", "Nachname","",true);
        Component cBirthdate = Component.of(3, ComponentType.DATE,  "birthdate", "Geburtsdatum","",true);
        Component cSignature = Component.of(99, ComponentType.SIGNATURE,  "signature", "Unterschrift","",true);

        eventApplicationService.saveComponent(id, cFirstName);
        eventApplicationService.saveComponent(id, cLastName);
        eventApplicationService.saveComponent(id, cBirthdate);
        eventApplicationService.saveComponent(id, cSignature);

    }

    private String getContent() {
        return "<table style=\"border-collapse: collapse; width: 100%;\" border=\"1\">\n" +
                "<colgroup>\n" +
                "<col style=\"width: 24.0045%;\">\n" +
                "<col style=\"width: 76.0694%;\">\n" +
                "</colgroup>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td><strong>Kategorie 1: Orte - Gebäude – Umgebungen</strong></td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>Gibt es bauliche oder organisatorische Aspekte, die Teilnehmende unwohl fühlen lassen könnten?</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>Werden die Toilettenräume für geschlechtergetrennte Nutzung ausgeschildert?</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>Sind die genutzten Räume öffentlich einsehbar?</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "\n" +
                "<p>&nbsp;</p>\n" +
                "\n" +
                "<table style=\"border-collapse: collapse; width: 100%; height: 231.6px;\" border=\"1\">\n" +
                "<colgroup>\n" +
                "<col style=\"width: 24.0045%;\">\n" +
                "<col style=\"width: 76.0694%;\">\n" +
                "</colgroup>\n" +
                "<tbody>\n" +
                "<tr style=\"height: 33.4px;\">\n" +
                "<td><strong>Kategorie 2: Awareness und Beschwerden</strong></td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr style=\"height: 92.2px;\">\n" +
                "<td>Werden zu Beginn der Aktion eine Begrüßungsrunde und Kennenlernphase angeboten? (Sofern sich die Teilnehmenden nicht kennen)</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr style=\"height: 33.4px;\">\n" +
                "<td>Werden Fotos gemacht?</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>Wie können Beschwerden oder Verbesserungsvorschläge geäußert werden?</td>\n" +
                "<td>\n" +
                "[ ] Abschlussrunde mit Feedback-Möglichkeit<br>\n" +
                "[ ] Telefonisch/E-Mail<br>\n" +
                "[ ] Soziale Medien (z. B. Instagram-Nachricht)\n" +
                "</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "\n" +
                "<p>&nbsp;</p>\n" +
                "\n" +
                "<table style=\"border-collapse: collapse; width: 100%; height: 86.4px;\" border=\"1\">\n" +
                "<colgroup>\n" +
                "<col style=\"width: 24.0045%;\">\n" +
                "<col style=\"width: 76.0694%;\">\n" +
                "</colgroup>\n" +
                "<tbody>\n" +
                "<tr style=\"height: 33.4px;\">\n" +
                "<td><strong>Kategorie 3: 1:1 Situationen</strong></td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr style=\"height: 53px;\">\n" +
                "<td>Können im Rahmen der Aktion 1:1-Situationen entstehen? Wenn ja, wo?</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "\n" +
                "<p>&nbsp;</p>\n" +
                "\n" +
                "<table style=\"border-collapse: collapse; width: 100%; height: 86.4px;\" border=\"1\">\n" +
                "<colgroup>\n" +
                "<col style=\"width: 24.0045%;\">\n" +
                "<col style=\"width: 76.0694%;\">\n" +
                "</colgroup>\n" +
                "<tbody>\n" +
                "<tr style=\"height: 33.4px;\">\n" +
                "<td><strong>Kategorie 4: Macht und Abhängigkeit</strong></td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr style=\"height: 53px;\">\n" +
                "<td>Bestehen hierarchische Abhängigkeiten zwischen Beteiligten der Aktion? Wenn ja, zwischen welchen Gruppen? Können besondere Abhängigkeitsverhältnisse entstehen?</td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "\n" +
                "<p>&nbsp;</p>\n" +
                "\n" +
                "<table style=\"border-collapse: collapse; width: 100%; height: 86.4px;\" border=\"1\">\n" +
                "<colgroup>\n" +
                "<col style=\"width: 24.0045%;\">\n" +
                "<col style=\"width: 76.0694%;\">\n" +
                "</colgroup>\n" +
                "<tbody>\n" +
                "<tr style=\"height: 33.4px;\">\n" +
                "<td><strong>Kategorie 5: Kommunikation, Beteiligung, Transparenz</strong></td>\n" +
                "<td>&nbsp;</td>\n" +
                "</tr>\n" +
                "<tr style=\"height: 53px;\">\n" +
                "<td>Welche Kommunikationswege werden für die Aktion genutzt?</td>\n" +
                "<td>\n" +
                "[ ] E-Mail<br>\n" +
                "[ ] Homepage<br>\n" +
                "[ ] Instagram<br>\n" +
                "[ ] WhatsApp\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>Wie werden Teilnehmende über Entscheidungsprozesse innerhalb der Aktion informiert?</td>\n" +
                "<td>\n" +
                "[ ] Ergebnisprotokolle<br>\n" +
                "[ ] Besprechungen/Versammlungen<br>\n" +
                "[ ] Veröffentlichung auf digitalen Kanälen<br>\n" +
                "[ ] Direkte Kommunikation\n" +
                "</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "\n" +
                "<p>&nbsp;</p>\n";
    }

}
