package com.messdiener.cms.workflow.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_form_definitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormDefinitionEntity {

    @Id
    @Column(name = "form_definition_id", nullable = false, length = 36)
    private UUID formDefinitionId; // :contentReference[oaicite:30]{index=30}

    @Column(name = "form_key", length = 255, nullable = false)
    private String key; // :contentReference[oaicite:31]{index=31}

    @Column(name = "version", nullable = false)
    private Integer version; // :contentReference[oaicite:32]{index=32}

    @Column(name = "form_name", length = 255, nullable = false)
    private String formName; // :contentReference[oaicite:33]{index=33}

    @Lob @Column(name = "form_description", columnDefinition = "LONGTEXT")
    private String formDescription; // :contentReference[oaicite:34]{index=34}

    @Column(name = "form_img", length = 1024)
    private String formImg; // :contentReference[oaicite:35]{index=35}

    @Lob @Column(name = "json_schema", columnDefinition = "LONGTEXT")
    private String jsonSchema; // :contentReference[oaicite:36]{index=36}

    @Lob @Column(name = "ui_schema", columnDefinition = "LONGTEXT")
    private String uiSchema; // :contentReference[oaicite:37]{index=37}

    @Lob @Column(name = "validations_json", columnDefinition = "LONGTEXT")
    private String validationsJson; // :contentReference[oaicite:38]{index=38}

    @Lob @Column(name = "output_templates_json", columnDefinition = "LONGTEXT")
    private String outputTemplatesJson; // :contentReference[oaicite:39]{index=39}
}
