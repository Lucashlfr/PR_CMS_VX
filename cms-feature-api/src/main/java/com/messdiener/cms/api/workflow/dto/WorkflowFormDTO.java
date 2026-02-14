package com.messdiener.cms.api.workflow.dto;

import com.messdiener.cms.shared.enums.workflow.CMSState;

import java.util.UUID;

public record WorkflowFormDTO(
        UUID moduleId,
        UUID workflowId,
        String uniqueName,     // z.B. DATA, SAE, APPROVAL ...
        String name,           // Anzeige-Name (getName())
        String description,    // Anzeige-Text (getDescription())
        String img,            // Illustration (getImg())
        CMSState state,
        int number,
        String creationDate,
        String modificationDate
) {}
