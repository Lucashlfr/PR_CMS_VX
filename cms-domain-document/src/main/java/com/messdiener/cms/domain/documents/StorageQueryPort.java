// src/main/java/com/messdiener/cms/domain/documents/StorageQueryPort.java
package com.messdiener.cms.domain.documents;
import java.util.List;
import java.util.UUID;

public interface StorageQueryPort {
    List<StorageFileView> getFiles(UUID personId);
}