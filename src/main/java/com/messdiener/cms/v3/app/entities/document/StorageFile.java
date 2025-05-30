package com.messdiener.cms.v3.app.entities.document;

import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.utils.time.CMSDate;
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
