package com.messdiener.cms.domain.document;

import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleQueryPort {
    List<ArticleView> getArticles() throws SQLException;

    List<ArticleView> getArticlesByType(ArticleType type, ArticleState state) throws SQLException;

    Optional<ArticleView> getArticleByType(ArticleType type) throws SQLException;

    Optional<ArticleView> getArticleById(UUID id) throws SQLException;
}
