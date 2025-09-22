package com.messdiener.cms.workflow.domain.entity.data;

import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;
@AllArgsConstructor
@Data
public class FormSubmission {

    private UUID id;
    private UUID workflowId;

    private String formKey;
    private int formVersion;

    private UUID submittedBy;
    private CMSDate submittedAt;

    private String checksum;
    private Map<String, Object> payload;
}
