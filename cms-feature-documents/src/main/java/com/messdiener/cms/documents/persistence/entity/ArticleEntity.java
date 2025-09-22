// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\entity\ArticleEntity.java
package com.messdiener.cms.documents.persistence.entity;

import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Abbild von module_articles
 * Alt-Schema (JDBC):
 *  id VARCHAR(36),
 *  tag INT AUTO_INCREMENT PRIMARY KEY,
 *  creator VARCHAR(36),
 *  lastUpdate long,
 *  target VARCHAR(36),
 *  articleState VARCHAR(255),
 *  articleType VARCHAR(255),
 *  title TEXT, description TEXT, imgUrl TEXT,
 *  html LONGBLOB, form LONGBLOB. :contentReference[oaicite:0]{index=0}
 */
@Entity
@Table(name = "module_articles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArticleEntity {

    /** Primärschlüssel der Tabelle ist 'tag' (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag", nullable = false)
    private Integer tag;

    /** Fachliche ID aus der Domäne. In der Alt-DB kein PK, aber eindeutig verwendet. */
    @Column(name = "id", length = 36, nullable = false)
    private UUID articleId;

    @Column(name = "creator", length = 36, nullable = false)
    private UUID creator;

    @Column(name = "lastUpdate", nullable = false)
    private Long lastUpdate; // epoch millis

    @Column(name = "target", length = 36, nullable = false)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(name = "articleState", length = 255, nullable = false)
    private ArticleState articleState;

    @Enumerated(EnumType.STRING)
    @Column(name = "articleType", length = 255, nullable = false)
    private ArticleType articleType;

    @Lob
    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "imgUrl")
    private String imgUrl;

    @Lob
    @Column(name = "html")
    private String html;

    @Lob
    @Column(name = "form")
    private String form;
}
