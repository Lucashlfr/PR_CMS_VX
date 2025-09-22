package com.messdiener.cms.documents.app.adapters;

import com.messdiener.cms.domain.documents.DocumentQueryPort;
import com.messdiener.cms.domain.documents.DocumentView;
import com.messdiener.cms.documents.persistence.service.DocumentService;
import com.messdiener.cms.documents.domain.entity.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentQueryAdapter implements DocumentQueryPort {

    private final DocumentService documentService;

    @Override
    public List<DocumentView> getAllDocumentsByTarget(String targetId) {
        try {
            List<Document> docs = documentService.getAllDocumentsByTarget(targetId);
            return docs.stream()
                    .map(d -> new DocumentView(
                            d.getId().toString(),
                            d.getTitle(),
                            "/document?id=" + d.getId()   // interner Link zu deiner View
                    ))
                    .toList();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load documents for target " + targetId, e);
        }
    }
}
