// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\map\EventMapper.java
package com.messdiener.cms.events.persistence.map;

import com.messdiener.cms.events.domain.entity.Event;
import com.messdiener.cms.events.persistence.entity.EventEntity;
import com.messdiener.cms.shared.enums.event.EventState;
import com.messdiener.cms.shared.enums.event.EventType;
import com.messdiener.cms.shared.enums.tenant.Tenant;
import com.messdiener.cms.utils.time.CMSDate;

public final class EventMapper {

    private EventMapper() {}

    public static Event toDomain(EventEntity e) {
        return new Event(
                e.getEventId(),
                Tenant.valueOf(e.getTenant()),
                e.getNumber() == null ? 0 : e.getNumber(),
                e.getTitle(),
                e.getDescription(),
                EventType.valueOf(e.getEventType()),
                EventState.valueOf(e.getEventState()),
                CMSDate.of(nullSafe(e.getStartDate())),
                CMSDate.of(nullSafe(e.getEndDate())),
                CMSDate.of(nullSafe(e.getDeadline())),
                CMSDate.of(defaultNow(e.getCreationDate())),
                CMSDate.of(defaultNow(e.getResubmission())),
                CMSDate.of(defaultNow(e.getLastUpdate())),
                e.getSchedule(),
                e.getRegistrationRelease(),
                e.getTargetGroup(),
                e.getLocation(),
                e.getImgUrl(),
                e.getRiskIndex() == null ? -1 : e.getRiskIndex(),
                e.getCurrentEditor(),
                e.getCreatedBy(),
                e.getPrincipal(),
                e.getManager(),
                e.getExpenditure() == null ? 0.0 : e.getExpenditure(),
                e.getRevenue() == null ? 0.0 : e.getRevenue(),
                e.getPressRelease(),
                e.getPreventionConcept(),
                e.getNotes(),
                e.getApplication(),
                e.getForms()
        );
    }

    public static EventEntity toEntity(Event d) {
        return EventEntity.builder()
                .eventId(d.getEventId())
                .tenant(d.getTenant().toString())
                .number(d.getNumber())
                .title(d.getTitle())
                .description(d.getDescription())
                .eventType(d.getType().toString())
                .eventState(d.getState().toString())
                .startDate(d.getStartDate().toLong())
                .endDate(d.getEndDate().toLong())
                .deadline(d.getDeadline().toLong())
                .creationDate(d.getCreationDate().toLong())
                .resubmission(d.getResubmission().toLong())
                .lastUpdate(d.getLastUpdate() != null ? d.getLastUpdate().toLong() : System.currentTimeMillis())
                .schedule(d.getSchedule())
                .registrationRelease(d.getRegistrationRelease())
                .targetGroup(d.getTargetGroup())
                .location(d.getLocation())
                .imgUrl(d.getImgUrl())
                .riskIndex(d.getRiskIndex())
                .currentEditor(d.getCurrentEditor())
                .createdBy(d.getCreatedBy())
                .principal(d.getPrincipal())
                .manager(d.getManager())
                .expenditure(d.getExpenditure())
                .revenue(d.getRevenue())
                .pressRelease(d.getPressRelease())
                .preventionConcept(d.getPreventionConcept())
                .notes(d.getNotes())
                .application(d.getApplication())
                .forms(d.getForms())
                .build();
    }

    private static long nullSafe(Long v){ return v == null ? 0L : v; }
    private static long defaultNow(Long v){ return v == null || v == 0 ? System.currentTimeMillis() : v; }
}
