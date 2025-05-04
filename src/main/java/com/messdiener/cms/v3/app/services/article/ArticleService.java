package com.messdiener.cms.v3.app.services.article;

import com.messdiener.cms.v3.app.entities.acticle.Article;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.ArticleState;
import com.messdiener.cms.v3.shared.enums.ArticleType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_articles (id VARCHAR(36),  tag INT AUTO_INCREMENT PRIMARY KEY, creator VARCHAR(36), lastUpdate long, target VARCHAR(36),  articleState VARCHAR(255), articleType VARCHAR(255), title TEXT, description TEXT, imgUrl TEXT, html LONGBLOB, form LONGBLOB)")) {
            preparedStatement.executeUpdate();
            LOGGER.info("Configuration table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }

        try{

            for(ArticleType type : ArticleType.values()){
                if(!databaseService.exists("module_articles", "articleType", type.toString())){
                    if(!type.isAutoCreate())continue;
                    Article article = Article.empty();
                    article.setArticleState(ArticleState.CREATED);
                    article.setArticleType(type);
                    article.setTitle(type.getLabel());
                    saveArticle(article);
                }
            }

        }catch (SQLException ex){
            throw new RuntimeException(ex);
        }
    }

    private Article getArticle(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        int tag = resultSet.getInt("tag");
        UUID creator = UUID.fromString(resultSet.getString("creator"));
        CMSDate lastUpdate = CMSDate.of(resultSet.getLong("lastUpdate"));
        String target = resultSet.getString("target");

        ArticleState articleState = ArticleState.valueOf(resultSet.getString("articleState"));
        ArticleType articleType = ArticleType.valueOf(resultSet.getString("articleType"));

        String title = resultSet.getString("title");
        String description = resultSet.getString("description");

        String imgUrl = resultSet.getString("imgUrl");
        String html = resultSet.getString("html");
        String form = resultSet.getString("form");
        return new Article(id, tag, creator, lastUpdate,target, articleState, articleType, title, description, imgUrl, html, form);
    }

    public void saveArticle(Article article) throws SQLException {
        databaseService.delete("module_articles", "id", article.getId().toString());
        String sql ="INSERT INTO module_articles (id, creator, lastUpdate, target, articleState, articleType, title, description, imgUrl, html, form) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, article.getId().toString());
            preparedStatement.setString(2, article.getCreator().toString());
            preparedStatement.setLong(3,article.getLastUpdate().toLong());
            preparedStatement.setString(4,article.getTarget());
            preparedStatement.setString(5, article.getArticleState().toString());
            preparedStatement.setString(6, article.getArticleType().toString());
            preparedStatement.setString(7, article.getTitle());
            preparedStatement.setString(8, article.getDescription());
            preparedStatement.setString(9, article.getImgUrl());
            preparedStatement.setString(10, article.getHtml());
            preparedStatement.setString(11, article.getForm());
            preparedStatement.executeUpdate();
        }
    }

    public List<Article> getArticles() throws SQLException {
        List<Article> articles = new ArrayList<>();
        String sql ="SELECT * FROM module_articles ORDER BY lastUpdate DESC";
        try (Connection connection = databaseService.getConnection();  PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    articles.add(getArticle(resultSet));
                }
            }
        }
        return articles;
    }

    public List<Article> getArticlesByType(ArticleType type, ArticleState state) throws SQLException {
        List<Article> articles = new ArrayList<>();
        String sql ="SELECT * FROM module_articles WHERE articleType = ? AND articleState = ? ORDER BY lastUpdate DESC";
        try (Connection connection = databaseService.getConnection();  PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, type.toString());
            preparedStatement.setString(2, state.toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    articles.add(getArticle(resultSet));
                }
            }
        }
        return articles;
    }

    public Optional<Article> getArticleById(UUID id) throws SQLException {
        String sql ="SELECT * FROM module_articles WHERE id = ?";
        try (Connection connection = databaseService.getConnection();  PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, id.toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next() ? Optional.of(getArticle(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<Article> getArticleByType(ArticleType type) throws SQLException {
        String sql ="SELECT * FROM module_articles WHERE articleType = ? LIMIT 1";
        try (Connection connection = databaseService.getConnection();  PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, type.toString());
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                return resultSet.next() ? Optional.of(getArticle(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Article> getArticlesById(String string) throws SQLException {
        List<Article> articles = new ArrayList<>();
        String sql ="SELECT * FROM module_articles WHERE target = ? ORDER BY lastUpdate DESC";
        try (Connection connection = databaseService.getConnection();  PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, string);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    articles.add(getArticle(resultSet));
                }
            }
        }
        return articles;
    }
}
