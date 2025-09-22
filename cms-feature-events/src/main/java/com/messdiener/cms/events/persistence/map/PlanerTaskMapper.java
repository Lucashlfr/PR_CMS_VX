// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\persistence\map\PlanerTaskMapper.java
package com.messdiener.cms.events.persistence.map;

import com.messdiener.cms.events.domain.entity.PlanerTask;
import com.messdiener.cms.events.persistence.entity.PlanerTaskEntity;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

public final class PlanerTaskMapper {

    private PlanerTaskMapper() {}

    public static PlanerTask toDomain(PlanerTaskEntity e) {
        CMSState state = e.getTaskState() != null ? e.getTaskState() : CMSState.ACTIVE;
        long created = e.getCreated() != null ? e.getCreated() : System.currentTimeMillis();
        long updated = e.getUpdated() != null ? e.getUpdated() : created;
        String lable = e.getLable() != null ? e.getLable() : "";
        return new PlanerTask(
                e.getTaskId(),
                e.getTaskNumber() != null ? e.getTaskNumber() : 0,
                e.getTaskName(),
                e.getTaskDescription(),
                state,
                CMSDate.of(created),
                CMSDate.of(updated),
                lable
        );
    }

    public static PlanerTaskEntity toEntity(UUID planerId, PlanerTask d) {
        return PlanerTaskEntity.builder()
                .taskId(d.getTaskId())
                .planerId(planerId)
                .taskNumber(d.getTaskNumber())
                .taskName(d.getTaskName())
                .taskDescription(d.getTaskDescription())
                .taskState(d.getState())
                .created(d.getCreated() != null ? d.getCreated().toLong() : System.currentTimeMillis())
                .updated(d.getUpdated() != null ? d.getUpdated().toLong() : System.currentTimeMillis())
                .lable(d.getLable())
                .build();
    }
}
