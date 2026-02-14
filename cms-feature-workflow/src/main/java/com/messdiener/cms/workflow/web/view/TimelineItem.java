// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\TimelineItem.java
package com.messdiener.cms.workflow.web.view;

import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.ElementType;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

/**
 * Normalisiertes Timeline-Element für das Workflow-Interface.
 * Alle Quellen (Tasks, Dokumentationen, Form-Submissions, Notifications, Artifacts, Audit)
 * werden darauf gemappt und gemeinsam chronologisch sortiert.
 */
public record TimelineItem(
        ElementType type,
        MessageType messageType,
        CMSState state,
        String title,
        String description,
        UUID currentUser,
        UUID creator,
        CMSDate creationDate,
        CMSDate modificationDate,
        int sLevel,
        String link
) {
}
