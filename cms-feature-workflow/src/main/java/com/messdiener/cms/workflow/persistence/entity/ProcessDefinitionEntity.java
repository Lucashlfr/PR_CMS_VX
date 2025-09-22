package com.messdiener.cms.workflow.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import com.messdiener.cms.shared.enums.workflow.CMSState;

import java.util.UUID;

@Entity
@Table(name = "wf_process_definitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessDefinitionEntity {

    @Id
    @Column(name = "process_definition_id", nullable = false, length = 36)
    private UUID processDefinitionId; // Domain: processDefinitionId :contentReference[oaicite:23]{index=23}

    @Column(name = "process_key", length = 255, nullable = false)
    private String key; // Domain: key :contentReference[oaicite:24]{index=24}

    @Column(name = "name", length = 255, nullable = false)
    private String name; // Domain: name :contentReference[oaicite:25]{index=25}

    @Column(name = "version", nullable = false)
    private Integer version; // Domain: version :contentReference[oaicite:26]{index=26}

    @Lob
    @Column(name = "states_json", columnDefinition = "LONGTEXT")
    private String statesJson; // Domain: List<CMSState> states -> JSON :contentReference[oaicite:27]{index=27}

    @Lob
    @Column(name = "transitions_json", columnDefinition = "LONGTEXT")
    private String transitionsJson; // Domain: Map<CMSState,CMSState> transitions -> JSON :contentReference[oaicite:28]{index=28}

    @Lob
    @Column(name = "sla_policies_json", columnDefinition = "LONGTEXT")
    private String slaPoliciesJson; // Domain: Map<CMSState,Long> slaPolicies -> JSON :contentReference[oaicite:29]{index=29}
}
