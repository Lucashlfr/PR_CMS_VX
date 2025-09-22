// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\service\WorkflowServiceV2.java
package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.FormSubmissionDTO;
import com.messdiener.cms.workflow.domain.dto.WorkflowDTO;
import com.messdiener.cms.workflow.persistence.entity.FormDefinitionEntity;
import com.messdiener.cms.workflow.persistence.entity.ProcessDefinitionEntity;
import com.messdiener.cms.workflow.persistence.map.WorkflowDtoMapper;
import com.messdiener.cms.workflow.persistence.repo.WorkflowRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowServiceV2 {

    private final WorkflowRepositoryV2 repo;
    private final FormSubmissionService formSubmissionService; // <<< NEU

    /* -----------------------------------------------------
     * Basis-CRUD / Query
     * --------------------------------------------------- */

    public WorkflowDTO save(WorkflowDTO dto) {
        var saved = repo.save(WorkflowDtoMapper.toEntity(dto));
        return WorkflowDtoMapper.toDto(saved);
    }

    public Optional<WorkflowDTO> getById(UUID id) {
        return repo.findById(id).map(WorkflowDtoMapper::toDto);
    }

    public List<WorkflowDTO> getAll() {
        return repo.findAllByOrderByCreationDateDesc()
                .stream().map(WorkflowDtoMapper::toDto).collect(Collectors.toList());
    }

    public List<WorkflowDTO> getByState(CMSState state) {
        return repo.findByStateOrderByCreationDateDesc(state)
                .stream().map(WorkflowDtoMapper::toDto).collect(Collectors.toList());
    }

    public List<WorkflowDTO> getByAssignee(UUID assigneeId) {
        return repo.findByAssigneeIdOrderByCreationDateDesc(assigneeId)
                .stream().map(WorkflowDtoMapper::toDto).collect(Collectors.toList());
    }

    public List<WorkflowDTO> getByApplicant(UUID applicantId) {
        return repo.findByApplicantIdOrderByCreationDateDesc(applicantId)
                .stream().map(WorkflowDtoMapper::toDto).collect(Collectors.toList());
    }

    public List<WorkflowDTO> getByProcess(String processKey, int version) {
        return repo.findByProcessKeyAndProcessVersionOrderByCreationDateDesc(processKey, version)
                .stream().map(WorkflowDtoMapper::toDto).collect(Collectors.toList());
    }

    public List<WorkflowDTO> searchByTitle(String queryPart) {
        return repo.findByTitleContainingIgnoreCaseOrderByCreationDateDesc(queryPart)
                .stream().map(WorkflowDtoMapper::toDto).collect(Collectors.toList());
    }

    /* -----------------------------------------------------
     * Convenience: createWorkflow – neue Controller-Signatur
     * --------------------------------------------------- */

    /**
     * Erstellt für jede Ziel-Person einen Workflow und liefert die erzeugten IDs zurück.
     * Zusätzlich werden pro ausgewählter FormDefinition Draft-FormSubmissions angelegt.
     */
    public List<UUID> createWorkflow(
            WorkflowType type,
            String label,
            String description,
            List<UUID> targetPersonIds,
            LocalDate startDate,
            LocalDate endDate,
            ProcessDefinitionEntity process,
            String initialState,
            List<FormDefinitionEntity> forms,
            boolean enableAutomation
    ) {
        Objects.requireNonNull(process, "process must not be null");
        var processKey = process.getKey();
        var processVersion = Optional.ofNullable(process.getVersion()).orElse(1);

        var now = CMSDate.of(System.currentTimeMillis());
        var end = localDateToCmsDateOrNull(endDate);
        var state = resolveState(initialState);

        var ids = new ArrayList<UUID>();
        if (targetPersonIds == null || targetPersonIds.isEmpty()) {
            // Kein Ziel -> leeren Workflow (ohne Assignee) erzeugen
            var wid = persistOne(null, label, description, processKey, processVersion, state, now, end);
            ids.add(wid);
            // Drafts ohne bekannten submittedBy
            createInitialSubmissions(wid, forms, null);
            return ids;
        }

        for (UUID assignee : targetPersonIds) {
            var wid = persistOne(assignee, label, description, processKey, processVersion, state, now, end);
            ids.add(wid);
            createInitialSubmissions(wid, forms, assignee);
        }
        return ids;
    }

    /* -----------------------------------------------------
     * Convenience: createWorkflow – ältere AdminController-Signatur
     * --------------------------------------------------- */

    /**
     * Kompatibilitäts-Overload für ältere Aufrufe aus dem AdminController.
     * Legt Draft-FormSubmissions an und gibt die erzeugte Workflow-ID zurück.
     */
    public UUID createWorkflow(
            String type,
            String label,
            String description,
            String personId,
            LocalDate startDate,
            LocalDate endDate,
            ProcessDefinitionEntity process,
            String initialState,
            List<FormDefinitionEntity> forms,
            boolean enabled,
            UUID createdBy
    ) {
        Objects.requireNonNull(process, "process must not be null");
        var processKey = process.getKey();
        var processVersion = Optional.ofNullable(process.getVersion()).orElse(1);

        var now = CMSDate.of(System.currentTimeMillis());
        var end = localDateToCmsDateOrNull(endDate);
        var state = resolveState(initialState);

        UUID assignee = null;
        if (StringUtils.hasText(personId)) {
            try {
                assignee = UUID.fromString(personId);
            } catch (IllegalArgumentException ignore) {
                // invalid personId -> bleibt null
            }
        }

        var wid = persistOne(assignee, label, description, processKey, processVersion, state, now, end);

        // Draft-Submissions direkt anlegen (submittedBy = assignee oder creator)
        createInitialSubmissions(wid, forms, assignee != null ? assignee : createdBy);
        return wid;
    }

    /* -----------------------------------------------------
     * intern
     * --------------------------------------------------- */

    private UUID persistOne(UUID assigneeId,
                            String title,
                            String description,
                            String processKey,
                            int processVersion,
                            CMSState state,
                            CMSDate now,
                            CMSDate endDate) {

        var dto = new WorkflowDTO(
                UUID.randomUUID(),            // workflowId
                processKey,                   // processKey
                processVersion,               // processVersion
                nullToEmpty(title),           // title
                nullToEmpty(description),     // description
                state,                        // state
                assigneeId,                   // assigneeId
                assigneeId,                   // applicantId (hier gleichgesetzt – alternativ: aktueller User)
                null,                         // manager
                null,                         // targetElement
                0,                            // priority
                List.of(),                    // tags
                0,                            // attachments
                0,                            // notes
                now,                          // creationDate
                now,                          // modificationDate
                endDate,                      // endDate
                0,                            // currentNumber
                ""                            // metadata
        );

        return save(dto).workflowId();
    }

    private void createInitialSubmissions(UUID workflowId,
                                          List<FormDefinitionEntity> forms,
                                          UUID submittedBy) {
        if (forms == null || forms.isEmpty()) return;
        for (FormDefinitionEntity f : forms) {
            // Ausschnitt aus WorkflowServiceV2.createInitialSubmissions(...)
            var dto = new FormSubmissionDTO(
                    UUID.randomUUID(),
                    workflowId,
                    f.getKey(),
                    Optional.ofNullable(f.getVersion()).orElse(1),
                    CMSState.OPEN,                              // <-- NEU
                    submittedBy,
                    CMSDate.of(System.currentTimeMillis()),
                    null,
                    Map.of()
            );
            formSubmissionService.save(dto);
        }
    }

    private static CMSDate localDateToCmsDateOrNull(LocalDate ld) {
        if (ld == null) return null;
        var millis = ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return CMSDate.of(millis);
    }

    private static CMSState resolveState(String stateText) {
        if (!StringUtils.hasText(stateText)) return CMSState.OPEN;
        try {
            return CMSState.valueOf(stateText.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return CMSState.OPEN;
        }
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }
}
