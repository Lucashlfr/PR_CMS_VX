package com.messdiener.cms.domain.documents;

import com.messdiener.cms.shared.enums.document.FileType;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * POJO-View mit JavaBean-Gettern, damit Thymeleaf-Aufrufe wie file.getTitle() funktionieren.
 * Spiegelt die Domain-Entity com.messdiener.cms.documents.domain.entity.StorageFile wider.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageFileView {

    private UUID id;
    private int tag;
    private UUID owner;
    private UUID target;
    private CMSDate lastUpdate;
    private String title;
    private CMSDate date;
    private double meta;
    private FileType type;
    private String path;

    /** Bequemer Link für Downloads/Inline-Ansicht, optional. */
    public String getUrl() {
        return "/file?id=" + id;
    }
}
