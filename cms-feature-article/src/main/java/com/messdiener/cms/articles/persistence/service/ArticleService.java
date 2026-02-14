// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\app\service\ArticleService.java
package com.messdiener.cms.articles.persistence.service;

import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import com.messdiener.cms.articles.domain.entity.Article;
import com.messdiener.cms.articles.persistence.entity.ArticleEntity;
import com.messdiener.cms.articles.persistence.map.ArticleMapper;
import com.messdiener.cms.articles.persistence.repo.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA-Refactor des bisherigen JDBC-Services; öffentliche Methoden bleiben identisch,
 * damit Adapter/Ports weiter funktionieren. :contentReference[oaicite:7]{index=7} :contentReference[oaicite:8]{index=8}
 */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);

    private final ArticleRepository repository;

    // --- Alt: @PostConstruct mit CREATE TABLE + Auto-Seed je ArticleType --- //
    // Das DDL entfällt (Flyway). Die Auto-Anlage je Type übernehmen wir hier weiter,
    // allerdings ohne SQLException-Signaturen. :contentReference[oaicite:9]{index=9}
    @Transactional
    public void ensureDefaultsForTypes() {
        for (ArticleType type : ArticleType.values()) {
            if (!repository.existsByArticleType(type)) {
                if (!type.isAutoCreate()) continue;
                Article a = Article.empty();
                a.setArticleState(ArticleState.CREATED);
                a.setArticleType(type);
                a.setTitle(type.getLabel());
                saveArticle(a);
            }
        }
    }

    // --- Öffentliche API (Signaturen wie zuvor genutzt) --- //

    @Transactional
    public void saveArticle(Article article) {
        // Alt: delete by id + insert. Wir prüfen auf vorhandenen Datensatz mit gleicher fachlicher UUID
        // und übernehmen dessen 'tag' (PK), um ein Update statt Neu-Insert zu erzwingen. :contentReference[oaicite:10]{index=10}
        Optional<ArticleEntity> existing = repository.findByArticleId(article.getId());
        ArticleEntity entity = ArticleMapper.toEntity(article);
        existing.ifPresent(e -> entity.setTag(e.getTag()));
        repository.save(entity);
        LOGGER.info("Article saved: id={}, tag={}", entity.getArticleId(), entity.getTag());
    }

    @Transactional(readOnly = true)
    public List<Article> getArticles() {
        return repository.findAllByOrderByLastUpdateDesc()
                .stream().map(ArticleMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:11]{index=11}
    }

    @Transactional(readOnly = true)
    public List<Article> getArticlesByType(ArticleType type, ArticleState state) {
        return repository.findByArticleTypeAndArticleStateOrderByLastUpdateDesc(type, state)
                .stream().map(ArticleMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:12]{index=12}
    }

    @Transactional(readOnly = true)
    public Optional<Article> getArticleById(UUID id) {
        return repository.findByArticleId(id).map(ArticleMapper::toDomain); // :contentReference[oaicite:13]{index=13}
    }

    @Transactional(readOnly = true)
    public Optional<Article> getArticleByType(ArticleType type) {
        return repository.findFirstByArticleType(type).map(ArticleMapper::toDomain); // :contentReference[oaicite:14]{index=14}
    }

    /** Altname im Service: getArticlesById(String) → lädt über Target-Feld. */
    @Transactional(readOnly = true)
    public List<Article> getArticlesById(String target) {
        return repository.findByTargetOrderByLastUpdateDesc(target)
                .stream().map(ArticleMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:15]{index=15}
    }
}
