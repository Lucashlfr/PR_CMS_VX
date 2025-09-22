package com.messdiener.cms.workflow.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wf_automation_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AutomationRuleEntity {

    @Id
    @Column(name = "rule_id", nullable = false, length = 36)
    private UUID ruleId; // Domain: ruleId :contentReference[oaicite:46]{index=46}

    @Column(name = "name", length = 255, nullable = false)
    private String name; // Domain: name :contentReference[oaicite:47]{index=47}

    @Column(name = "trigger_type", length = 64, nullable = false)
    private String triggerType; // Domain: triggerType :contentReference[oaicite:48]{index=48}

    @Column(name = "trigger_config", length = 512)
    private String triggerConfig; // Domain: triggerConfig :contentReference[oaicite:49]{index=49}

    @Lob @Column(name = "condition_json", columnDefinition = "LONGTEXT")
    private String conditionJson; // Domain: conditionJson :contentReference[oaicite:50]{index=50}

    @Lob @Column(name = "actions_json", columnDefinition = "LONGTEXT")
    private String actionsJson; // Domain: actionsJson :contentReference[oaicite:51]{index=51}

    @Column(name = "enabled", nullable = false)
    private boolean enabled; // Domain: enabled :contentReference[oaicite:52]{index=52}
}
