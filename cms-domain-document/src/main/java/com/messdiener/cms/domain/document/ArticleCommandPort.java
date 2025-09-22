package com.messdiener.cms.domain.document;

import java.sql.SQLException;

public interface ArticleCommandPort {
    void saveArticle(ArticleView article) throws SQLException;
}
