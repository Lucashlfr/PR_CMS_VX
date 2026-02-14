// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\entity\ArticleEntity.java
package com.messdiener.cms.articles.persistence.entity;

import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "title", columnDefinition = "LONGTEXT")
    private String title;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "imgUrl", columnDefinition = "LONGTEXT")
    private String imgUrl;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "html", columnDefinition = "LONGTEXT")
    private String html;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "form", columnDefinition = "LONGTEXT")
    private String form;
}
