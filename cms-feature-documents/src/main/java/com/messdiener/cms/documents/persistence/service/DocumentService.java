// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\app\service\DocumentService.java
package com.messdiener.cms.documents.persistence.service;

import com.messdiener.cms.documents.domain.entity.Document;
import com.messdiener.cms.documents.persistence.map.DocumentMapper;
import com.messdiener.cms.documents.persistence.repo.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;

    // Alte @PostConstruct-DDL wird entfernt (Hibernate übernimmt Schema-Validierung/Aufbau)

    private Document mapThrowableToSql(SQLExceptionSupplier<Document> action) throws SQLException {
        try {
            return action.get();
        } catch (RuntimeException ex) {
            LOGGER.error("Document operation failed", ex);
            throw new SQLException("Document operation failed", ex);
        }
    }

    private <T> T mapThrowableToSql(SqlSupplier<T> action) throws SQLException {
        try {
            return action.get();
        } catch (RuntimeException ex) {
            LOGGER.error("Document operation failed", ex);
            throw new SQLException("Document operation failed", ex);
        }
    }

    @FunctionalInterface
    private interface SQLExceptionSupplier<T> { T get() throws SQLException; }

    @FunctionalInterface
    private interface SqlSupplier<T> { T get(); }

    /**
     * Ersetzt das frühere DELETE+INSERT durch ein einfaches save().
     */
    public void saveDocument(Document document) throws SQLException {
        mapThrowableToSql(() -> {
            documentRepository.save(DocumentMapper.toEntity(document));
            return null;
        });
    }

    /**
     * Beibehaltung der Signatur (String), da der DocumentQueryAdapter diese aufruft.
     * Konvertiert intern in UUID und lädt per Repository.
     */
    public List<Document> getAllDocumentsByTarget(String target) throws SQLException {
        return mapThrowableToSql(() ->
                documentRepository.findByTarget(UUID.fromString(target))
                        .stream()
                        .map(DocumentMapper::toDomain)
                        .collect(toList())
        );
    }

    public Optional<Document> getDocument(UUID id) throws SQLException {
        return mapThrowableToSql(() ->
                documentRepository.findById(id).map(DocumentMapper::toDomain)
        );
    }
}
