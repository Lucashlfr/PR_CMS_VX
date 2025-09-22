// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\map\ArticleMapper.java
package com.messdiener.cms.documents.persistence.map;

import com.messdiener.cms.documents.domain.entity.Article;
import com.messdiener.cms.documents.persistence.entity.ArticleEntity;
import com.messdiener.cms.utils.time.CMSDate;

public final class ArticleMapper {

    private ArticleMapper(){}

    public static Article toDomain(ArticleEntity e) {
        return new Article(
                e.getArticleId(),
                e.getTag() == null ? 0 : e.getTag(),
                e.getCreator(),
                CMSDate.of(e.getLastUpdate() == null ? 0L : e.getLastUpdate()),
                e.getTarget(),
                e.getArticleState(),
                e.getArticleType(),
                e.getTitle(),
                e.getDescription(),
                e.getImgUrl(),
                e.getHtml(),
                e.getForm()
        );
    }

    public static ArticleEntity toEntity(Article d) {
        return ArticleEntity.builder()
                .tag(d.getTag() == 0 ? null : d.getTag())
                .articleId(d.getId())
                .creator(d.getCreator())
                .lastUpdate(d.getLastUpdate() != null ? d.getLastUpdate().toLong() : System.currentTimeMillis())
                .target(d.getTarget())
                .articleState(d.getArticleState())
                .articleType(d.getArticleType())
                .title(d.getTitle())
                .description(d.getDescription())
                .imgUrl(d.getImgUrl())
                .html(d.getHtml())
                .form(d.getForm())
                .build();
    }
}
