package com.messdiener.cms.v3.shared.enums.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum WorkflowType {
    ONBOARDING("Onboarding","Dieser Workflow umfasst wichtige erste Schritte, die alle neuen Nutzer*innen abschließen müssen. Dazu gehört die Bestätigung, dass du mit der Verarbeitung deiner Daten gemäß der DSGVO einverstanden bist, sowie die Aktualisierung grundlegender persönlicher Informationen wie Name, Adresse und Geburtsdatum. Außerdem bitten wir dich, mindestens eine Kontaktperson – idealerweise deine Eltern – zu hinterlegen, die im Notfall informiert werden kann.","/img/wf-onboarding.png"),
    SAE("Selbstauskunftserklärung","Die Selbstauskunftserklärung ist eine verpflichtende Erklärung für alle, die innerhalb der katholischen Kirche Verantwortung übernehmen möchten. Mit deiner Unterschrift bestätigst du, dass du entsprechend den kirchlichen Vorgaben für diese Aufgabe geeignet bist.","/img/wf_sae.png"),
    FINANCE("Rechnung einreichen","Nutze diesen Workflow, um Rechnungen oder Auslagen zur Erstattung einzureichen. Hier kannst du alle erforderlichen Informationen angeben sowie Belege hochladen, damit deine Ausgaben geprüft und anschließend erstattet werden können.",""),
    NULL("NULL", "", "");

    private final String label;
    private final String description;
    private final String imgUrl;
}
