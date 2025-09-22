// X:\workspace\PR_CMS\cms-feature-documents\src\main\java\com\messdiener\cms\documents\persistence\map\DocumentMapper.java
package com.messdiener.cms.documents.persistence.map;

import com.messdiener.cms.documents.domain.entity.Document;
import com.messdiener.cms.documents.persistence.entity.DocumentEntity;
import com.messdiener.cms.utils.time.CMSDate;

public final class DocumentMapper {

    private DocumentMapper() {}

    public static Document toDomain(DocumentEntity e) {
        return new Document(
                e.getId(),
                e.getOwner(),
                e.getTarget(),
                CMSDate.of(e.getLastUpdate() == null ? 0L : e.getLastUpdate()),
                e.getTitle(),
                e.getContent(),
                e.getPermissions()
        );
    }

    public static DocumentEntity toEntity(Document d) {
        DocumentEntity e = new DocumentEntity();
        e.setId(d.getId());
        e.setOwner(d.getOwner());
        e.setTarget(d.getTarget());
        e.setLastUpdate(d.getLastUpdate() == null ? System.currentTimeMillis() : d.getLastUpdate().toLong());
        e.setTitle(d.getTitle());
        e.setContent(d.getContent());
        e.setPermissions(d.getPermissions());
        return e;
    }
}
