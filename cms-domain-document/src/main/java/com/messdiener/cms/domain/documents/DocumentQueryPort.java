package com.messdiener.cms.domain.documents;
import java.util.List;

public interface DocumentQueryPort {
    List<DocumentView> getAllDocumentsByTarget(String targetId);
}
