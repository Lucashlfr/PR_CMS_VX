// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\entity\PlanerTaskEntity.java
package com.messdiener.cms.events.persistence.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "module_planer_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanerTaskEntity {

    @Id
    @Column(name = "taskId", length = 36, nullable = false)
    private UUID taskId; // Domain-ID

    @Column(name = "planerId", length = 36, nullable = false)
    private UUID planerId; // Event-ID als Fremdschlüssel (ohne FK-Constraint)

    @Column(name = "id")
    private Integer taskNumber; // Auto-Increment im Alt-Schema, hier als Feld geführt

    @Lob
    @Column(name = "taskName")
    private String taskName;

    @Lob
    @Column(name = "taskDescription")
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "taskState", length = 255, nullable = false)
    private CMSState taskState;

    @Column(name = "created")
    private Long created;

    @Column(name = "updated")
    private Long updated;

    @Column(name = "lable", length = 255)
    private String lable;
}
