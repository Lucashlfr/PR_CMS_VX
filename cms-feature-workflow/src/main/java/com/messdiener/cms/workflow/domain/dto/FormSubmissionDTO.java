// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\domain\dto\FormSubmissionDTO.java
package com.messdiener.cms.workflow.domain.dto;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.Map;
import java.util.UUID;

public record FormSubmissionDTO(
        UUID id,
        UUID workflowId,
        String formKey,
        int formVersion,
        CMSState state,       // <-- NEU
        UUID submittedBy,
        CMSDate submittedAt,
        String checksum,
        Map<String, Object> payload
) {}
