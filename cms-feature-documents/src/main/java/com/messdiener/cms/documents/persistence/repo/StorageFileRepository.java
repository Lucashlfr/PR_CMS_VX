// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\repo\StorageFileRepository.java
package com.messdiener.cms.documents.persistence.repo;

import com.messdiener.cms.documents.persistence.entity.StorageFileEntity;
import com.messdiener.cms.shared.enums.document.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface StorageFileRepository extends JpaRepository<StorageFileEntity, Integer> {

    // SELECT * FROM module_storage WHERE target=? AND meta != 0
    List<StorageFileEntity> findByTargetAndMetaNot(UUID target, Double metaZero); // :contentReference[oaicite:1]{index=1}

    // SELECT COALESCE(SUM(meta),0) FROM module_storage WHERE target=?
    @Query("select coalesce(sum(s.meta),0) from StorageFileEntity s where s.target = ?1")
    double sumMetaByTarget(UUID target); // :contentReference[oaicite:2]{index=2}

    // SELECT * FROM module_storage WHERE target=? AND type=?
    List<StorageFileEntity> findByTargetAndType(UUID target, FileType type); // :contentReference[oaicite:3]{index=3}

    // SELECT * FROM module_storage WHERE target=?
    List<StorageFileEntity> findByTarget(UUID target); // :contentReference[oaicite:4]{index=4}

    // SELECT * FROM module_storage WHERE documentId=?
    Optional<StorageFileEntity> findByDocumentId(UUID documentId); // :contentReference[oaicite:5]{index=5}
}
