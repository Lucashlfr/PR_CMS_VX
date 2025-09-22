package com.messdiener.cms.workflow.domain.entity.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class FormDefinition {

    private UUID formDefinitionId;
    private String key;

    private int version;

    private String formName;
    private String formDescription;
    private String formImg;

    private String jsonSchema;
    private String uiSchema;
    private String validationsJson;
    private String outputTemplatesJson; // Templates für PDF etc.

}
