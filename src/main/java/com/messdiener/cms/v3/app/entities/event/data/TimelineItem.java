package com.messdiener.cms.v3.app.entities.event.data;

import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class TimelineItem {

    private UUID id;
    private String title;
    private String description;
    private CMSDate date;

}
