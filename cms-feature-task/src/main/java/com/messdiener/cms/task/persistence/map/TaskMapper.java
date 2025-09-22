// X:\workspace\PR_CMS\cms-feature-task\src\main\java\com\messdiener\cms\task\persistence\map\TaskMapper.java
package com.messdiener.cms.task.persistence.map;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.task.domain.entity.task.Task;
import com.messdiener.cms.task.persistence.entity.TaskEntity;
import com.messdiener.cms.utils.time.CMSDate;

public final class TaskMapper {

    private TaskMapper(){}

    public static Task toDomain(TaskEntity e){
        return new Task(
                e.getTaskId(),
                e.getNumber() != null ? e.getNumber() : 0,
                e.getLink(),
                e.getCreator(),
                e.getEditor(),
                CMSDate.of(nz(e.getLastUpdate())),
                CMSDate.of(nz(e.getCreationDate())),
                CMSDate.of(nz(e.getEndDate())),
                e.getTitle(),
                e.getDescription(),
                CMSState.valueOf(e.getTaskState()),
                e.getPriority()
        );
    }

    public static TaskEntity toEntity(Task d){
        return TaskEntity.builder()
                // Nummer kommt vom Domainobjekt (Altcode setzt sie im INSERT). :contentReference[oaicite:5]{index=5}
                .number(d.getNumber())
                .taskId(d.getTaskId())
                .link(d.getLink())
                .creator(d.getCreator())
                .editor(d.getEditor())
                .lastUpdate(System.currentTimeMillis()) // Altcode: lastUpdate = now() beim Speichern. :contentReference[oaicite:6]{index=6}
                .creationDate(d.getCreationDate() != null ? d.getCreationDate().toLong() : 0L)
                .endDate(d.getEndDate() != null ? d.getEndDate().toLong() : 0L)
                .title(d.getTitle())
                .description(d.getDescription())
                .taskState(d.getTaskState().toString())
                .priority(d.getPriority())
                .build();
    }

    private static long nz(Long v){ return v == null ? 0L : v; }
}
