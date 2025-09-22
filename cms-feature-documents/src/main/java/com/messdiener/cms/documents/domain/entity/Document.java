package com.messdiener.cms.documents.domain.entity;

import com.messdiener.cms.utils.time.CMSDate;
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
