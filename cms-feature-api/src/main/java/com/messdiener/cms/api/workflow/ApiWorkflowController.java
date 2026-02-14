package com.messdiener.cms.api.workflow;

import com.messdiener.cms.api.common.response.ApiResponse;
import com.messdiener.cms.api.common.response.ApiResponseHelper;
import com.messdiener.cms.api.workflow.dto.WorkflowFormDTO;
import com.messdiener.cms.domain.workflow.WorkflowFormView;
import com.messdiener.cms.domain.workflow.WorkflowQueryPort;
import com.messdiener.cms.domain.workflow.WorkflowSummaryDTO;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.domain.person.PersonSessionView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping(value = "/api/workflows", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiWorkflowController {

    private final WorkflowQueryPort workflowQueryPort;
    private final SecurityHelper securityHelper;
    private final WorkflowQueryPort formQueryPort;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<WorkflowSummaryDTO>>> dashboard(
            HttpServletRequest request,
            Locale locale
    ) {
        UUID userId = securityHelper.getPerson()
                .map(PersonSessionView::id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WorkflowSummaryDTO> items = workflowQueryPort.getWorkflowsByUserId(userId);
        return ApiResponseHelper.ok(items, request, locale);
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> count(
            HttpServletRequest request,
            Locale locale
    ) {
        String personId = securityHelper.getPerson()
                .map(p -> p.id().toString())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int count = workflowQueryPort.countRelevantWorkflows(personId);
        return ApiResponseHelper.ok(count, request, locale);
    }

    @GetMapping("/forms")
    public ResponseEntity<ApiResponse<List<WorkflowFormDTO>>> getForms(
            HttpServletRequest request,
            Locale locale
    ) {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<WorkflowFormDTO> data = formQueryPort.getActiveFormsForUser(sessionUser.id())
                .stream()
                .map(ApiWorkflowController::toDto)
                .toList();

        return ApiResponseHelper.ok(data, request, locale);
    }

    private static WorkflowFormDTO toDto(WorkflowFormView v) {
        return new WorkflowFormDTO(
                v.moduleId(),
                v.workflowId(),
                v.uniqueName(),
                v.name(),
                v.description(),
                v.img(),
                v.state(),
                v.number(),
                v.creationDate(),
                v.modificationDate()
        );
    }
}
