package com.messdiener.cms.documents.app.adapters;

import com.messdiener.cms.domain.document.ArticleQueryPort;
import com.messdiener.cms.domain.document.ArticleView;
import com.messdiener.cms.documents.persistence.service.ArticleService;
import com.messdiener.cms.documents.domain.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class ArticleQueryAdapter implements ArticleQueryPort {

    private final ArticleService articleService;

    @Override
    public List<ArticleView> getArticles() throws SQLException {
        return articleService.getArticles().stream().map(this::map).collect(toList());
    }

    @Override
    public List<ArticleView> getArticlesByType(
            com.messdiener.cms.shared.enums.ArticleType type,
            com.messdiener.cms.shared.enums.ArticleState state) throws SQLException {
        return articleService.getArticlesByType(type, state).stream().map(this::map).collect(toList());
    }

    @Override
    public Optional<ArticleView> getArticleByType(com.messdiener.cms.shared.enums.ArticleType type) throws SQLException {
        return articleService.getArticleByType(type).map(this::map);
    }

    @Override
    public Optional<ArticleView> getArticleById(UUID id) throws SQLException {
        return articleService.getArticleById(id).map(this::map);
    }

    private ArticleView map(Article a) {
        return new ArticleView(
                a.getId(),
                a.getTag(),
                a.getCreator(),
                a.getLastUpdate(),
                a.getTarget(),
                a.getArticleState(),
                a.getArticleType(),
                a.getTitle(),
                a.getDescription(),
                a.getImgUrl(),
                a.getForm(),
                a.getHtml()
        );
    }
}
