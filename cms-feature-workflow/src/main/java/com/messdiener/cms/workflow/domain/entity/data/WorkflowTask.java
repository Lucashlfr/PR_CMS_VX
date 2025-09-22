package com.messdiener.cms.workflow.domain.entity.data;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor @Data
public class WorkflowTask {

    private UUID id;
    private UUID workflowId;

    private String key;
    private String title;
    private CMSState state;

    private List<String> candidateRoles;
    private UUID assigneeId;

    private CMSDate dueAt;
    private CMSDate createdAt;

    private Map<String, Object> payload;


}
