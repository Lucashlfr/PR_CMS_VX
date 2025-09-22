// X:\workspace\PR_CMS\cms-feature-task\src\main\java\com\messdiener\cms\task\persistence\repo\TaskRepository.java
package com.messdiener.cms.task.persistence.repo;

import com.messdiener.cms.task.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {

    List<TaskEntity> findByLinkOrderByNumberAsc(UUID link); // SELECT * WHERE link=? ORDER BY number (stabile Reihenfolge) :contentReference[oaicite:2]{index=2}

    Optional<TaskEntity> findByTaskId(UUID taskId); // SELECT * WHERE taskId=? :contentReference[oaicite:3]{index=3}

    void deleteByTaskId(UUID taskId); // DELETE WHERE taskId=? (Altcode macht delete+insert) :contentReference[oaicite:4]{index=4}
}
