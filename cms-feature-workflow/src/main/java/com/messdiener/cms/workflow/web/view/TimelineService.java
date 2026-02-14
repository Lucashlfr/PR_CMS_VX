// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\TimelineService.java
package com.messdiener.cms.workflow.web.view;

import com.messdiener.cms.audit.persistence.service.AuditService;
import com.messdiener.cms.shared.enums.MessageType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.ElementType;
import com.messdiener.cms.workflow.persistence.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final AuditService auditService;
    private final FormService formService;

    /**
     * Baut eine gemischte Timeline für einen Workflow auf und sortiert sie absteigend nach Zeit.
     */
    public List<TimelineItem> buildForWorkflow(UUID workflowId) {
/*
        // Dokumentationen
        Stream<TimelineItem> docs = documentationService.getByWorkflow(workflowId).stream()
                .map(d -> new TimelineItem(
                        nz(d.createdAt()),
                        ElementType.DOCUMENTATION,
                        d.documentationId(),
                        d.workflowId(),
                        s(coalesce(d.title(), "Dokument")),
                        s(d.description()),
                        CMSState.INFO,
                        MessageType.NULL,
                        d.assigneeId(),
                        "/workflow/documentation?id=" + d.documentationId()
                ));

        // Form Submissions


        // Artifacts (z. B. Uploads)
        Stream<TimelineItem> artifacts = artifactService.getByWorkflow(workflowId).stream()
                .map(a -> new TimelineItem(
                        fromEpoch(a.getCreatedAt()),
                        ElementType.ARTIFACT,
                        a.getArtifactId(),
                        a.getWorkflowId(),
                        "Artifact: " + coalesce(a.getArtifactType(), "?"),
                        "",
                        CMSState.INFO,
                        MessageType.NULL,
                        a.getCreatedBy(),
                        null
                ));
*/
        // Audit Logs
        Stream<TimelineItem> audit = auditService.getLogsByConnectId(workflowId).stream()
                .map(a -> new TimelineItem(
                        ElementType.AUDIT_TRAIL,
                        a.getType(),
                        CMSState.NULL,
                        a.getTitle(),
                        a.getDescription(),
                        null,
                        a.getUserId(),
                        a.getTimestamp(),
                        a.getTimestamp(),
                        0,
                        null
                ));

        Stream<TimelineItem> submissions = formService.getWorkflowModulesByWorkflowId(workflowId).stream()
                .map(f -> new TimelineItem(
                        ElementType.FORM_SUBMISSION,
                        MessageType.NULL,
                        f.getState(),
                        "Formular: " + f.getName() + " · v" + 1,
                        "",
                        f.getCurrentUser(),
                        f.getCreator(),
                        f.getCreationDate(),
                        f.getModificationDate(),
                        0,
                        (f.getState() == CMSState.COMPLETED) ? null : "/workflow/form?id=" + f.getModuleId()
                ));

        // Zusammenführen + sortieren (neueste zuerst)
        return Stream.of(audit, submissions)
                .flatMap(s -> s)
                .filter(Objects::nonNull)
                .sorted(Comparator.<TimelineItem, Long>comparing(
                        i -> i.modificationDate() != null ? i.modificationDate().toLong() : 0L
                ).reversed()).toList();
    }
}
