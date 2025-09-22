// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\entity\DocumentEntity.java
package com.messdiener.cms.documents.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "module_document")
@Getter
@Setter
@NoArgsConstructor
public class DocumentEntity {

    @Id
    @Column(name = "documentId", length = 36, nullable = false)
    private UUID id;

    @Column(name = "owner", length = 36, nullable = false)
    private UUID owner;

    @Column(name = "target", length = 36, nullable = false)
    private UUID target;

    @Column(name = "lastUpdate", nullable = false)
    private Long lastUpdate;

    @Lob
    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "permissions")
    private String permissions;
}
