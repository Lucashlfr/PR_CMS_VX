package com.messdiener.cms.documents.domain.entity;

import com.messdiener.cms.shared.enums.document.FileType;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class StorageFile {

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

}
