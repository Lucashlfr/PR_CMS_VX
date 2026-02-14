package com.messdiener.cms.workflow.app.adapters;

import com.messdiener.cms.domain.workflow.WorkflowFormView;
import com.messdiener.cms.domain.workflow.WorkflowQueryPort;
import com.messdiener.cms.domain.workflow.WorkflowSummaryDTO;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.workflow.persistence.service.FormService;
import com.messdiener.cms.workflow.persistence.service.WorkflowService;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkflowQueryAdapter implements WorkflowQueryPort {

    private final WorkflowService workflowService;
    private final PersonHelper personHelper;
    private final FormService formService;

    @Override
    public int countRelevantWorkflows(String personId) {
        return workflowService.getRelevantWorkflows(personId).size();
    }

    @Override
    public List<WorkflowSummaryDTO> getWorkflowsByUserId(UUID userId) {
        List<Workflow> list = workflowService.getWorkflowsByUserId(userId);
        return list.stream()
                .map(w -> new WorkflowSummaryDTO(
                        w.getWorkflowId(),
                        w.getWorkflowName(),
                        w.getWorkflowDescription(),
                        w.getCategory(),
                        w.getState(),
                        personHelper.getName(w.getEditor()),
                        personHelper.getName(w.getCreator()),
                        personHelper.getName(w.getManager()),
                        0,
                        0,
                        w.getCreationDate(),
                        w.getModificationDate(),
                        w.getEndDate(),
                        w.getCurrentNumber()
                ))
                .toList();
    }

    @Override
    public List<WorkflowFormView> getActiveFormsForUser(UUID current) {
        return formService.getActiveFormsForUser(current).stream().map(f ->
                new WorkflowFormView(
                        f.getModuleId(), f.getWorkflowId(), f.getUniqueName().name(),
                        f.getName(), f.getDescription(), f.getImg(),
                        f.getState(), f.getNumber(),
                        f.getCreationDate().getGermanTime(), f.getModificationDate().getGermanTime()
                )
        ).toList();
    }

}
