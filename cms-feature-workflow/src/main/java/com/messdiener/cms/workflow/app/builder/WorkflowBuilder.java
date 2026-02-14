package com.messdiener.cms.workflow.app.builder;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.shared.enums.workflow.WorkflowCategory;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class WorkflowBuilder {

    private final WorkflowType workflowType;
    private final List<UUID> usersIds;

    private UUID workflowId;
    private String workflowName;
    private String workflowDescription;

    private WorkflowCategory category;
    private CMSState state = CMSState.OPEN;

    private UUID editorId;
    private UUID creatorId;
    private UUID managerId;
    private UUID targetId;

    private int attachments = 0;
    private int notes = 0;

    private CMSDate creationDate;
    private CMSDate modificationDate;
    private CMSDate startDate;
    private CMSDate endDate;

    private int currentNumber = 1;
    private String metadata = "";

    public WorkflowBuilder(WorkflowType workflowType, UUID userId) {
        this.workflowId = UUID.randomUUID();
        this.workflowType = workflowType;
        this.usersIds = List.of(userId);
    }

    public WorkflowBuilder(WorkflowType workflowType, List<UUID> usersIds) {
        this.workflowId = UUID.randomUUID();
        this.workflowType = workflowType;
        this.usersIds = usersIds;
    }

    public WorkflowBuilder setCreator(UUID creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public WorkflowBuilder setEditor(UUID editorId) {
        this.editorId = editorId;
        return this;
    }

    public WorkflowBuilder setManager(UUID managerId) {
        this.managerId = managerId;
        return this;
    }

    public WorkflowBuilder setTarget(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public WorkflowBuilder setWorkflowId(UUID workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public WorkflowBuilder setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
        return this;
    }

    public WorkflowBuilder setWorkflowDescription(String description) {
        this.workflowDescription = description;
        return this;
    }

    public WorkflowBuilder setCategory(WorkflowCategory category) {
        this.category = category;
        return this;
    }

    public WorkflowBuilder setState(CMSState state) {
        this.state = state;
        return this;
    }

    public WorkflowBuilder setAttachments(int attachments) {
        this.attachments = attachments;
        return this;
    }

    public WorkflowBuilder setNotes(int notes) {
        this.notes = notes;
        return this;
    }

    public WorkflowBuilder setCreationDate(CMSDate creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public WorkflowBuilder setModificationDate(CMSDate modificationDate) {
        this.modificationDate = modificationDate;
        return this;
    }

    public WorkflowBuilder setStartDate(CMSDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public WorkflowBuilder setEndDate(CMSDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public WorkflowBuilder setCurrentNumber(int currentNumber) {
        this.currentNumber = currentNumber;
        return this;
    }

    public WorkflowBuilder setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public Workflow build() {
        final UUID defaultTarget = (targetId != null)
                ? targetId
                : (usersIds != null && !usersIds.isEmpty() ? usersIds.get(0) : null);

        final UUID creator = (this.creatorId != null) ? this.creatorId : defaultTarget;
        final UUID manager = (this.managerId != null) ? this.managerId : creator;
        final UUID editor = (this.editorId != null) ? this.editorId : creator;

        final CMSDate created = (this.creationDate != null) ? this.creationDate : CMSDate.current();
        final CMSDate modified = (this.modificationDate != null) ? this.modificationDate : created;

        final String name = (this.workflowName != null && !this.workflowName.isBlank())
                ? this.workflowName
                : (this.workflowType != null ? this.workflowType.name() : "Workflow");

        final WorkflowCategory cat = this.category;

        return new Workflow(
                this.workflowId,
                name,
                this.workflowDescription,
                cat,
                this.state,
                editor,
                creator,
                manager,
                defaultTarget,
                this.attachments,
                this.notes,
                created,
                modified,
                this.endDate,
                this.currentNumber,
                this.metadata
        );
    }
}
