package com.messdiener.cms.events.domain.entity.data;

import com.messdiener.cms.shared.enums.event.EventMessageType;
import com.messdiener.cms.utils.time.CMSDate;
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
