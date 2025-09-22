package com.messdiener.cms.workflow.domain.entity.configuration;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Data
public class AutomationRule {

    private UUID ruleId;
    private String name;

    private String triggerType;     // onEvent, onSchedule, onCondition
    private String triggerConfig;   // z.B. CRON Ausdruck oder Event-Key

    private String conditionJson;   // Logik (JSONLogic, SpEL)
    private String actionsJson;     // Aktionen in JSON

    private boolean enabled;
}
