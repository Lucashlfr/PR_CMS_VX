package com.messdiener.cms.workflow.domain.entity.configuration;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Data
public class ProcessDefinition {

    private UUID processDefinitionId;
    private String key;
    private String name;
    private int version;

    private List<CMSState> states;                 // Zustände im Ablauf
    private Map<CMSState, CMSState> transitions;     // Übergänge state->state
    private Map<CMSState, Long> slaPolicies;       // SLA je State (z.B. Stunden/ Tage)
}
