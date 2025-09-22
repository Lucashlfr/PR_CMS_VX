package com.messdiener.cms.documents.app.adapters;

import com.messdiener.cms.documents.app.service.StorageService;
import com.messdiener.cms.domain.documents.StorageFileView;
import com.messdiener.cms.domain.documents.StorageQueryPort;
import com.messdiener.cms.documents.domain.entity.StorageFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageQueryAdapter implements StorageQueryPort {

    private final StorageService storageService;

    @Override
    public List<StorageFileView> getFiles(UUID personId) {
        List<StorageFile> files = storageService.getFiles(personId);
        return files.stream()
                .map(f -> new StorageFileView(
                        f.getTitle() != null && !f.getTitle().isBlank() ? f.getTitle() : f.getId().toString(),
                        // simple Download-/Open-URL; passe bei Bedarf an eine bestehende Route an:
                        "/img?id=" + f.getId(),
                        safeSizeOf(f.getPath())
                ))
                .toList();
    }

    private long safeSizeOf(String path) {
        if (path == null || path.isBlank()) return 0L;
        try {
            File file = storageService.loadFile(path); // nutzt deinen bestehenden Loader
            return file.length();
        } catch (Exception ignored) {
            return 0L;
        }
    }
}
