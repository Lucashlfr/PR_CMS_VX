// X:\workspace\PR_CMS\cms-feature-audit\src\main\java\com\messdiener\cms\audit\persistence\entity\AuditLogEntity.java
package com.messdiener.cms.audit.persistence.entity;

import com.messdiener.cms.shared.enums.ActionCategory;
import com.messdiener.cms.shared.enums.MessageInformationCascade;
import com.messdiener.cms.shared.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "module_audit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogEntity {

    @Id
    @Column(name = "log", length = 36, nullable = false)
    private UUID logId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 255, nullable = false)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 255, nullable = false)
    private ActionCategory category;

    @Column(name = "connectId", length = 255, nullable = false)
    private String connectId; // im Alt-Schema VARCHAR(255) :contentReference[oaicite:3]{index=3}

    @Column(name = "userId", length = 36, nullable = false)
    private UUID userId;

    @Column(name = "timestamp")
    private Long timestamp;

    @Lob
    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "mic", length = 3, nullable = false)
    private MessageInformationCascade mic;

    @Column(name = "file")
    private Boolean file;
}
