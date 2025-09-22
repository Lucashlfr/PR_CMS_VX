package com.messdiener.cms.workflow.domain.entity.data;

import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Data
public class AuditTrail {

    private UUID trailId;
    private UUID workflowId;

    private UUID actorId;
    private String action;

    private Map<String, Object> beforePayload;
    private Map<String, Object> afterPayload;

    private CMSDate date;
}
