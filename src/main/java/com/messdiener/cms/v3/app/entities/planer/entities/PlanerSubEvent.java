package com.messdiener.cms.v3.app.entities.planer.entities;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PlanerSubEvent {

    private UUID subId;
    private CMSDate lastModified;
    private UUID personId;
    private String title;
    private String html;

}
