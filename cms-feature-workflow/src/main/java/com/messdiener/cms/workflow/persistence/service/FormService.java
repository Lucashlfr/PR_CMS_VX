// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\app\service\WorkflowModuleService.java
package com.messdiener.cms.workflow.persistence.service;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowFormName;
import com.messdiener.cms.shared.ui.Component;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import com.messdiener.cms.workflow.persistence.entity.FormEntity;
import com.messdiener.cms.workflow.persistence.map.FormMapper;
import com.messdiener.cms.workflow.persistence.repo.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;

    public Optional<WorkflowForm> getWorkflowModuleById(UUID moduleId) {
        return formRepository.findById(moduleId).map(FormMapper::toDomain); // :contentReference[oaicite:27]{index=27}
    }

    public List<WorkflowForm> getWorkflowModulesByWorkflowId(UUID workflowId) {
        return formRepository.findByWorkflowIdOrderByNumberAsc(workflowId)
                .stream().map(FormMapper::toDomain).toList(); // :contentReference[oaicite:28]{index=28}
    }

    public void saveForm(WorkflowForm module) {
        formRepository.save(FormMapper.toEntity(module));
    }

    public void createForm(UUID workflowId, WorkflowFormName formName, CMSState state, int number, UUID editorId, UUID creatorId){
        WorkflowForm form = WorkflowForm.getWorkflowModule(
                workflowId,
                formName,
                UUID.randomUUID(),          // moduleId
                state != null ? state : CMSState.OPEN,
                number,
                CMSDate.current(),          // creationDate
                CMSDate.current(),          // modificationDate
                "{}",                       // results (leer)
                editorId,                   // currentUser
                creatorId                   // creator
        );
        formRepository.save(FormMapper.toEntity(form));
    }

    public Optional<WorkflowForm> getModuleByNumber(UUID workflowId, int currentNumber) {
        return formRepository.findByWorkflowIdAndNumber(workflowId, currentNumber)
                .map(FormMapper::toDomain); // :contentReference[oaicite:30]{index=30}
    }

    public int getWorkflowModuleCountByWorkflowId(UUID workflowId) {
        return formRepository.countByWorkflowId(workflowId); // :contentReference[oaicite:31]{index=31}
    }

    public void updateWorkflowModule(WorkflowForm form) {
        // Wir benötigen workflowId nicht; Entity enthält nur moduleId als PK.
        // Lade existierende Entity, überschreibe relevante Felder.
        FormEntity existing = formRepository.findById(form.getModuleId())
                .orElseThrow(() -> new NoSuchElementException("WorkflowModule not found: " + form.getModuleId()));
        existing.setCreatorId(form.getCreator());
        existing.setCurrentId(form.getCurrentUser());
        existing.setState(form.getState());
        existing.setCreationDate(form.getCreationDate().toLong());
        existing.setModificationDate(System.currentTimeMillis());
        existing.setNumber(form.getNumber());
        existing.setUniqueName(form.getUniqueName());
        existing.setMetaData(form.getResults());
        formRepository.save(existing); // :contentReference[oaicite:32]{index=32}
    }

    public UUID getWorkflowId(UUID moduleId) {
        return formRepository.findWorkflowIdByModuleId(moduleId)
                .orElseThrow(() -> new IndexOutOfBoundsException("Workflow not found.")); // :contentReference[oaicite:33]{index=33}
    }

    public UUID getCurrentModuleId(UUID workflowID) {
        return formRepository.findCurrentFormId(workflowID)
                .orElseThrow(() -> new IndexOutOfBoundsException("Workflow not found.")); // :contentReference[oaicite:34]{index=34}
    }

    // FormService#getActiveModulesForUserByWorkflow
    public List<WorkflowForm> getActiveModulesForUserByWorkflow(String userId, String workflowId) {
        UUID current = UUID.fromString(userId);
        UUID wf     = UUID.fromString(workflowId);
        List<FormEntity> rows = formRepository
                .findByCurrentIdAndWorkflowIdAndStateInOrderByNumberAsc(
                        current, wf, List.of(CMSState.OPEN, CMSState.ACTIVE));
        return rows.stream().map(FormMapper::toDomain).collect(Collectors.toList());
    }

    public List<WorkflowForm> getActiveFormsForUser(UUID current) {
        if (current == null) {
            return List.of();
        }

        // Nur Status OPEN oder WAITING laden (direkt aus der DB, sortiert nach letzter Änderung)
        var rows = formRepository.findByCurrentIdAndStateInOrderByModificationDateDesc(
                current,
                List.of(CMSState.OPEN, CMSState.WAITING)
        );

        // In Domain-Objekte (WorkflowForm) mappen
        return rows.stream()
                .map(FormMapper::toDomain)
                .toList();
    }
}
