package com.messdiener.cms.v3.app.services.document;


import com.messdiener.cms.v3.app.entities.document.Document;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
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
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS module_document (documentId VARCHAR(255), owner VARCHAR(255), target VARCHAR(255), lastUpdate long, title TEXT, content LONGTEXT, permissions VARCHAR(255))")) {
            preparedStatement.executeUpdate();
            LOGGER.info("Configuration table initialized successfully.");
        } catch (SQLException e) {
            LOGGER.error("Error while initializing configuration table", e);
            throw new RuntimeException(e);
        }
    }

    private Document getByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("documentId"));
        UUID owner = UUID.fromString(resultSet.getString("owner"));
        UUID target = UUID.fromString(resultSet.getString("target"));
        CMSDate lastUpdate = CMSDate.of(resultSet.getLong("lastUpdate"));
        String title = resultSet.getString("title");
        String content = resultSet.getString("content");
        String permissions = resultSet.getString("permissions");
        return new Document(id, owner, target, lastUpdate, title, content, permissions);
    }

    public void saveDocument(Document document) throws SQLException {
        databaseService.delete("module_document", "documentId", document.getId().toString());
        String sql = "INSERT INTO module_document (documentId, owner, target, lastUpdate, title, content, permissions) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, document.getId().toString());
            preparedStatement.setString(2, document.getOwner().toString());
            preparedStatement.setString(3, document.getTarget().toString());
            preparedStatement.setLong(4, document.getLastUpdate().toLong());
            preparedStatement.setString(5, document.getTitle());
            preparedStatement.setString(6, document.getContent());
            preparedStatement.setString(7, document.getPermissions());
            preparedStatement.executeUpdate();
        }
    }

    public List<Document> getAllDocumentsByTarget(String target) throws SQLException {
        List<Document> events = new ArrayList<>();
        String sql = "SELECT * FROM module_document WHERE target = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, target);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    events.add(getByResultSet(resultSet));
                }
            }
        }
        return events;
    }

    public Optional<Document> getDocument(UUID id) throws SQLException {
        String sql = "SELECT * FROM module_document WHERE documentId = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(getByResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }
}