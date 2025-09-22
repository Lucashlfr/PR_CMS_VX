// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\map\EventComponentMapper.java
package com.messdiener.cms.events.persistence.map;

import com.messdiener.cms.events.persistence.entity.EventComponentEntity;
import com.messdiener.cms.shared.enums.ComponentType;
import com.messdiener.cms.shared.ui.Component;

public final class EventComponentMapper {
    private EventComponentMapper(){}

    public static Component toDomain(EventComponentEntity e){
        Component c = new Component();
        c.setNumber(e.getNumber() == null ? 0 : e.getNumber());
        c.setType(ComponentType.valueOf(e.getType()));
        c.setName(e.getName());
        c.setLabel(e.getLabel());
        c.setValue(e.getValue());
        c.setOptions(e.getOptions());
        c.setRequired(Boolean.TRUE.equals(e.getRequired()));
        return c;
    }

    public static EventComponentEntity toEntity(java.util.UUID eventId, Component c){
        return EventComponentEntity.builder()
                .eventId(eventId)
                .number(c.getNumber())
                .type(c.getType().toString())
                .name(c.getName())
                .label(c.getLabel())
                .value(c.getValue())
                .options(c.getOptions())
                .required(c.isRequired())
                .build();
    }
}
