package com.messdiener.cms.documents.app.adapters;

import com.messdiener.cms.domain.document.ArticleCommandPort;
import com.messdiener.cms.domain.document.ArticleView;
import com.messdiener.cms.documents.persistence.service.ArticleService;
import com.messdiener.cms.documents.domain.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class ArticleCommandAdapter implements ArticleCommandPort {

    private final ArticleService articleService;

    @Override
    public void saveArticle(ArticleView v) throws SQLException {
        Article a = new Article(
                v.id(),
                v.tag(),
                v.creator(),
                v.lastUpdate(),
                v.target(),
                v.articleState(),
                v.articleType(),
                v.title(),
                v.description(),
                v.imgUrl(),
                v.html(),
                v.form()
        );
        // lastUpdate wird im Service gesetzt bzw. kommt aus dem Record (v.lastUpdate)
        a.setLastUpdate(v.lastUpdate());
        articleService.saveArticle(a);
    }
}
