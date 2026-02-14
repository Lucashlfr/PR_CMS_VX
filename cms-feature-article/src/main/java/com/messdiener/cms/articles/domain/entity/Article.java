package com.messdiener.cms.articles.domain.entity;

import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Article {

    private UUID id;
    private int tag;
    private UUID creator;
    private CMSDate lastUpdate;
    private String target;

    private ArticleState articleState;
    private ArticleType articleType;

    private String title;
    private String description;

    private String imgUrl;
    private String html;
    private String form;

    public static Article empty(){
        return new Article(UUID.randomUUID(), 0, Cache.SYSTEM_USER, CMSDate.current(), "", ArticleState.NULL, ArticleType.NULL, "", "", "", "", "");
    }

    public static Article of(UUID id, String title, ArticleType articleType, String target){
        return new Article(id,  0, Cache.SYSTEM_USER, CMSDate.current(), target, ArticleState.CREATED, articleType, title, "", "", "", "");
    }

}
