// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\map\StorageFileMapper.java
package com.messdiener.cms.files.persistence.map;

import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.files.domain.entity.StorageFile;
import com.messdiener.cms.files.persistence.entity.StorageFileEntity;

public final class StorageFileMapper {

    private StorageFileMapper(){}

    public static StorageFile toDomain(StorageFileEntity e) {
        return new StorageFile(
                e.getDocumentId(),
                e.getTag() == null ? 0 : e.getTag(),
                e.getOwner(),
                e.getTarget(),
                CMSDate.of(e.getLastUpdate() == null ? 0L : e.getLastUpdate()),
                e.getTitle(),
                CMSDate.of(e.getDate() == null ? 0L : e.getDate()),
                e.getMeta() == null ? 0.0 : e.getMeta(),
                e.getType(),
                e.getPath()
        );
    }

    public static StorageFileEntity toEntity(StorageFile d) {
        return StorageFileEntity.builder()
                .tag(d.getTag() == 0 ? null : d.getTag())
                .documentId(d.getId())
                .owner(d.getOwner())
                .target(d.getTarget())
                .lastUpdate(d.getLastUpdate() != null ? d.getLastUpdate().toLong() : System.currentTimeMillis())
                .title(d.getTitle())
                .date(d.getDate() != null ? d.getDate().toLong() : System.currentTimeMillis())
                .meta(d.getMeta())
                .type(d.getType())
                .path(d.getPath())
                .build();
    }
}
