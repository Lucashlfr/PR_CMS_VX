// X:\workspace\PR_CMS\cms-feature-task\src\main\java\com\messdiener\cms\task\persistence\entity\TaskEntity.java
package com.messdiener.cms.task.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskEntity {

    // Achtung: In der Alt-DB ist 'number' AUTO_INCREMENT PRIMARY KEY.
    // Wir setzen die Id bewusst manuell (KEIN @GeneratedValue), weil der Altcode die Nummer explizit schreibt. :contentReference[oaicite:1]{index=1}
    @Id
    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "taskId", length = 36, nullable = false, unique = true)
    private UUID taskId;

    @Column(name = "link", length = 36, nullable = false)
    private UUID link;

    @Column(name = "creator", length = 36, nullable = false)
    private UUID creator;

    @Column(name = "editor", length = 36, nullable = false)
    private UUID editor;

    @Column(name = "lastUpdate")
    private Long lastUpdate; // epoch ms

    @Column(name = "creationDate")
    private Long creationDate; // epoch ms

    @Column(name = "endDate")
    private Long endDate; // epoch ms

    @Lob
    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "taskState", length = 50)
    private String taskState;

    @Column(name = "priority", length = 10)
    private String priority;
}
