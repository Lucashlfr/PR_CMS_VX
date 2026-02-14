package com.messdiener.cms.workflow.domain.entity;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowCategory;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Workflow {

    private UUID workflowId;

    private String workflowName;
    private String workflowDescription;

    private WorkflowCategory category;
    private CMSState state;

    private UUID editor;
    private UUID creator;
    private UUID manager;
    private UUID target;

    private int attachments;
    private int notes;

    private CMSDate creationDate;
    private CMSDate modificationDate;
    private CMSDate endDate;

    private int currentNumber;
    private String metadata;

    public static Workflow empty(UUID workflowId, UUID editor, UUID creator, UUID manager, WorkflowType workflowType) {
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setWorkflowName("Workflow · " + workflowType.getLabel());
        workflow.setWorkflowDescription(workflowType.getDescription());
        workflow.setCategory(WorkflowCategory.ONB);
        workflow.setState(CMSState.ACTIVE);
        workflow.setEditor(editor);
        workflow.setCreator(creator);
        workflow.setManager(manager);
        workflow.setTarget(editor);
        workflow.setAttachments(0);
        workflow.setNotes(0);
        workflow.setCreationDate(CMSDate.current());
        workflow.setModificationDate(CMSDate.current());
        workflow.setEndDate(CMSDate.of(0));
        workflow.setCurrentNumber(0);
        workflow.setMetadata("");
        return workflow;
    }
}
