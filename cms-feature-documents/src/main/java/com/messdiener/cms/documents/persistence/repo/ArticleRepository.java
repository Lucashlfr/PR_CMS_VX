// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\repo\ArticleRepository.java
package com.messdiener.cms.documents.persistence.repo;

import com.messdiener.cms.documents.persistence.entity.ArticleEntity;
import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Integer> {

    // SELECT * ORDER BY lastUpdate DESC
    List<ArticleEntity> findAllByOrderByLastUpdateDesc(); // :contentReference[oaicite:1]{index=1}

    // SELECT * WHERE articleType=? AND articleState=? ORDER BY lastUpdate DESC
    List<ArticleEntity> findByArticleTypeAndArticleStateOrderByLastUpdateDesc(ArticleType type, ArticleState state); // :contentReference[oaicite:2]{index=2}

    // SELECT * WHERE articleType=? LIMIT 1
    Optional<ArticleEntity> findFirstByArticleType(ArticleType type); // :contentReference[oaicite:3]{index=3}

    // SELECT * WHERE id=?
    Optional<ArticleEntity> findByArticleId(UUID articleId); // :contentReference[oaicite:4]{index=4}

    // SELECT * WHERE target=? ORDER BY lastUpdate DESC
    List<ArticleEntity> findByTargetOrderByLastUpdateDesc(String target); // :contentReference[oaicite:5]{index=5}

    boolean existsByArticleType(ArticleType type); // für Auto-Initialisierung (Altcode nutzte DatabaseService.exists) :contentReference[oaicite:6]{index=6}
}
