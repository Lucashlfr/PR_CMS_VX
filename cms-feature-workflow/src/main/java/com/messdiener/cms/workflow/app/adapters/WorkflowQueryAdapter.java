package com.messdiener.cms.workflow.app.adapters;

import com.messdiener.cms.domain.workflow.WorkflowQueryPort;
import com.messdiener.cms.domain.workflow.WorkflowSummaryDTO;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.workflow.domain.dto.WorkflowDTO;
import com.messdiener.cms.workflow.persistence.service.WorkflowServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class WorkflowQueryAdapter implements WorkflowQueryPort {

    private final WorkflowServiceV2 workflowService;
    private final PersonHelper personHelper;

    @Override
    public int countRelevantWorkflows(String personId) {
        UUID pid = UUID.fromString(personId);

        // „Relevant“ = mir zugewiesen ∪ von mir beantragt (du kannst das bei Bedarf erweitern)
        var assigned = workflowService.getByAssignee(pid);
        var applied  = workflowService.getByApplicant(pid);

        // distinct nach workflowId
        var distinct = Stream.concat(assigned.stream(), applied.stream())
                .collect(Collectors.toMap(WorkflowDTO::workflowId, w -> w, (a, b) -> a))
                .values();

        return distinct.size();
    }

    @Override
    public List<WorkflowSummaryDTO> getWorkflowsByUserId(UUID userId) {
        // In der alten Version gab es getWorkflowsByUserId(); wir bilden das als
        // Union aus „assigned to user“ und „created/applicant = user“ nach.
        var assigned = workflowService.getByAssignee(userId);
        var applied  = workflowService.getByApplicant(userId);

        // distinct nach workflowId, stabile Einfügereihenfolge
        var list = new ArrayList<>(
                Stream.concat(assigned.stream(), applied.stream())
                        .collect(Collectors.toMap(WorkflowDTO::workflowId, w -> w, (a, b) -> a, LinkedHashMap::new))
                        .values()
        );

        return list.stream()
                .map(w -> new WorkflowSummaryDTO(
                        w.workflowId(),
                        w.title(),                                 // früher: workflowName
                        w.description(),                           // früher: workflowDescription
                        w.processKey(),                            // „category“ ~ Prozessschlüssel
                        w.state(),
                        nameOrEmpty(w.assigneeId()),               // editor
                        nameOrEmpty(w.applicantId()),              // creator
                        nameOrEmpty(w.manager()),                  // manager
                        w.attachments(),
                        w.notes(),
                        w.creationDate(),
                        w.modificationDate(),
                        w.endDate(),
                        w.currentNumber()
                ))
                .toList();
    }

    private String nameOrEmpty(UUID personId) {
        if (personId == null) return "";
        try {
            return personHelper.getName(personId);
        } catch (Exception e) {
            return "";
        }
    }
}
