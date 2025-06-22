package com.messdiener.cms.v3.app.entities.event.data;

import com.messdiener.cms.v3.shared.enums.event.EventMessageType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class MessageItem {

    private UUID id;
    private int number;
    private String title;
    private String description;
    private CMSDate date;
    private EventMessageType messageType;
    private UUID userId;

}
