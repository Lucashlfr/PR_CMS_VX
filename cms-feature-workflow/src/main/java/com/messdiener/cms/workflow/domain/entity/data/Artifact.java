package com.messdiener.cms.workflow.domain.entity.data;

import com.messdiener.cms.utils.time.CMSDate;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Artifact {

    private UUID artifactId;
    private UUID workflowId;

    private String artifactType; // PDF, UPLOAD, etc.

    private CMSDate createdAt;
    private UUID createdBy;
}
