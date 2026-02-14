package com.messdiener.cms.files.app.adapters;

import com.messdiener.cms.domain.documents.StorageFileView;
import com.messdiener.cms.domain.documents.StorageQueryPort;
import com.messdiener.cms.files.domain.entity.StorageFile;
import com.messdiener.cms.files.persistence.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageQueryAdapter implements StorageQueryPort {

    private final StorageService storageService;

    @Override
    public List<StorageFileView> getFiles(UUID personId) {
        List<StorageFile> files = storageService.getFiles(personId); // lädt Domain-Objekte
        return files.stream()
                .map(f -> new StorageFileView(
                        f.getId(),
                        f.getTag(),
                        f.getOwner(),
                        f.getTarget(),
                        f.getLastUpdate(),
                        f.getTitle(),
                        f.getDate(),
                        f.getMeta(),
                        f.getType(),
                        f.getPath()
                ))
                .toList();
    }
}
