// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\persistence\map\FormSubmissionMapper.java
package com.messdiener.cms.workflow.persistence.map;

import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.FormSubmissionDTO;
import com.messdiener.cms.workflow.persistence.entity.FormSubmissionEntity;

public final class FormSubmissionMapper {
    private FormSubmissionMapper() {}

    public static FormSubmissionDTO toDto(FormSubmissionEntity e) {
        return new FormSubmissionDTO(
                e.getId(),
                e.getWorkflowId(),
                e.getFormKey(),
                e.getFormVersion() != null ? e.getFormVersion() : 0,
                e.getState(), // <-- NEU
                e.getSubmittedBy(),
                CMSDate.of(e.getSubmittedAt() != null ? e.getSubmittedAt() : 0L),
                e.getChecksum(),
                Jsons.toMap(e.getPayloadJson())
        );
    }

    public static FormSubmissionEntity toEntity(FormSubmissionDTO d) {
        return FormSubmissionEntity.builder()
                .id(d.id())
                .workflowId(d.workflowId())
                .formKey(d.formKey())
                .formVersion(d.formVersion())
                .state(d.state()) // <-- NEU
                .submittedBy(d.submittedBy())
                .submittedAt(d.submittedAt() != null ? d.submittedAt().toLong() : System.currentTimeMillis())
                .checksum(d.checksum())
                .payloadJson(Jsons.toJson(d.payload()))
                .build();
    }
}
