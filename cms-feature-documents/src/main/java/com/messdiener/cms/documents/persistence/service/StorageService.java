// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\app\service\StorageService.java
package com.messdiener.cms.documents.app.service;

import com.messdiener.cms.documents.domain.entity.StorageFile;
import com.messdiener.cms.documents.persistence.entity.StorageFileEntity;
import com.messdiener.cms.documents.persistence.map.StorageFileMapper;
import com.messdiener.cms.documents.persistence.repo.StorageFileRepository;
import com.messdiener.cms.shared.enums.document.FileType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA-Refactor des bisherigen JDBC-Services:
 * - Entferntes @PostConstruct DDL (Schema via Flyway)
 * - Public API (Signaturen) beibehalten: store(..), load(..), loadFiles(..), getSumm(..), getFiles(..), getFile(..) etc. :contentReference[oaicite:6]{index=6} :contentReference[oaicite:7]{index=7}
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final StorageFileRepository repository;

    /* ===== File-System Teil bleibt wie gehabt ===== */

    public String store(MultipartFile receiptFile, StorageFile storageFile) {
        if (receiptFile.isEmpty()) {
            throw new StorageException("Leere Datei kann nicht gespeichert werden.");
        }

        String original = StringUtils.cleanPath(Objects.requireNonNull(receiptFile.getOriginalFilename()));
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > 0) ext = original.substring(idx);

        String filename = storageFile.getId() + ext;
        try {
            Path root = Paths.get("./cms_vx/finance/");
            Files.createDirectories(root);
            Path target = root.resolve(filename);

            try (InputStream in = receiptFile.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            storageFile.setPath(target + filename);
            store(storageFile); // persistieren
            return filename;
        } catch (IOException e) {
            throw new StorageException("Fehler beim Speichern der Datei " + filename, e);
        }
    }

    public void store(StorageFile storageFile) {
        StorageFileEntity entity = StorageFileMapper.toEntity(storageFile);
        repository.save(entity); // vormals INSERT via JDBC :contentReference[oaicite:8]{index=8}
    }

    public File load(String filename) {
        Path root = Paths.get("./cms_vx/finance/").toAbsolutePath().normalize();
        String[] exts = {".png", ".pdf", ".jpg"};
        for (String ext : exts) {
            Path filePath = root.resolve(filename + ext).normalize();
            File file = filePath.toFile();
            if (file.exists() && file.isFile()) {
                return file;
            }
        }
        throw new StorageException("Datei nicht gefunden: " + filename);
    }

    public File loadFile(String path) {
        Path filePath = Path.of(path);
        File file = filePath.toFile();
        if (file.exists() && file.isFile()) {
            return file;
        }
        throw new StorageException("Datei nicht gefunden: " + path);
    }

    /* ===== Datenbank-Operationen (jetzt JPA) ===== */

    public List<StorageFile> loadFiles(UUID target) {
        return repository.findByTargetAndMetaNot(target, 0.0d)
                .stream().map(StorageFileMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:9]{index=9}
    }

    public double getSumm(UUID target) {
        return repository.sumMetaByTarget(target); // :contentReference[oaicite:10]{index=10}
    }

    public List<StorageFile> getFiles(UUID id, FileType fileTypeE) {
        return repository.findByTargetAndType(id, fileTypeE)
                .stream().map(StorageFileMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:11]{index=11}
    }

    public List<StorageFile> getFiles(UUID id) {
        return repository.findByTarget(id)
                .stream().map(StorageFileMapper::toDomain).collect(Collectors.toList()); // :contentReference[oaicite:12]{index=12}
    }

    public Optional<StorageFile> getFile(UUID id) {
        return repository.findByDocumentId(id).map(StorageFileMapper::toDomain); // :contentReference[oaicite:13]{index=13}
    }
}
