package com.messdiener.cms.v3.app.entities.document;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Document {

    private UUID id;
    private UUID owner;
    private UUID target;
    private CMSDate lastUpdate;
    private String title;
    private String content;
    private String permissions;

}
