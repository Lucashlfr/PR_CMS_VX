// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\map\EventMessageMapper.java
package com.messdiener.cms.events.persistence.map;

import com.messdiener.cms.events.domain.entity.data.MessageItem;
import com.messdiener.cms.events.persistence.entity.EventMessageEntity;
import com.messdiener.cms.shared.enums.event.EventMessageType;
import com.messdiener.cms.utils.time.CMSDate;

public final class EventMessageMapper {
    private EventMessageMapper(){}

    public static MessageItem toDomain(EventMessageEntity e){
        return new MessageItem(
                e.getMessageId(),
                e.getNumber() == null ? 0 : e.getNumber(),
                e.getTitle(),
                e.getDescription(),
                CMSDate.of(e.getDate() == null ? 0L : e.getDate()),
                EventMessageType.valueOf(e.getType()),
                e.getUserId()
        );
    }

    public static EventMessageEntity toEntity(java.util.UUID eventId, MessageItem d){
        return EventMessageEntity.builder()
                .messageId(d.getId())
                .eventId(eventId)
                .number(d.getNumber())
                .title(d.getTitle())
                .description(d.getDescription())
                .date(d.getDate().toLong())
                .type(d.getMessageType().toString())
                .userId(d.getUserId())
                .build();
    }
}
