// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\entity\StorageFileEntity.java
package com.messdiener.cms.files.persistence.entity;

import com.messdiener.cms.shared.enums.document.FileType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Abbild von: module_storage
 * Alt-DDL (JDBC):
 *  documentId VARCHAR(255),
 *  tag INT AUTO_INCREMENT PRIMARY KEY,
 *  owner VARCHAR(255),
 *  target VARCHAR(255),
 *  lastUpdate long,
 *  title TEXT,
 *  date long,
 *  meta double,
 *  type VARCHAR(30),
 *  path TEXT. :contentReference[oaicite:0]{index=0}
 */
@Entity
@Table(name = "module_storage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StorageFileEntity {

    /** PK in der Tabelle ist 'tag' (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag", nullable = false)
    private Integer tag;

    @Column(name = "documentId", length = 255, nullable = false)
    private UUID documentId;

    @Column(name = "owner", length = 255, nullable = false)
    private UUID owner;

    @Column(name = "target", length = 255, nullable = false)
    private UUID target;

    @Column(name = "lastUpdate", nullable = false)
    private Long lastUpdate; // epoch millis

    @Lob
    @Column(name = "title")
    private String title;

    @Column(name = "date", nullable = false)
    private Long date; // epoch millis

    @Column(name = "meta", nullable = false)
    private Double meta;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private FileType type;

    @Lob
    @Column(name = "path")
    private String path;
}
