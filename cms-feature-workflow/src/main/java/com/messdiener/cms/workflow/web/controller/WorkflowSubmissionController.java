// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\controller\WorkflowSubmissionController.java
package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.workflow.domain.dto.FormSubmissionDTO;
import com.messdiener.cms.workflow.domain.dto.WorkflowDTO;
import com.messdiener.cms.workflow.persistence.entity.FormDefinitionEntity;
import com.messdiener.cms.workflow.persistence.repo.FormDefinitionRepository;
import com.messdiener.cms.workflow.persistence.service.FormSubmissionService;
import com.messdiener.cms.workflow.persistence.service.WorkflowServiceV2;
import com.messdiener.cms.workflow.web.view.ComponentView;
import com.messdiener.cms.workflow.web.view.ModuleView;
import com.messdiener.cms.workflow.web.view.StepView;
import com.messdiener.cms.workflow.web.view.builder.FormSchemaComponentBuilder;
import com.messdiener.cms.workflow.web.view.payload.SubmissionPayloadService;
import com.messdiener.cms.workflow.persistence.service.WorkflowAuditTrailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WorkflowSubmissionController {

    private final FormSubmissionService submissionService;
    private final WorkflowServiceV2 workflowService;
    private final FormDefinitionRepository formDefRepo;

    private final FormSchemaComponentBuilder componentBuilder;
    private final SubmissionPayloadService payloadService;
    private final WorkflowAuditTrailService auditTrailService; // NEU

    @GetMapping("/workflow/submission")
    public String showSubmission(@RequestParam("id") String submissionIdStr, Model model) {
        UUID submissionId = UUID.fromString(submissionIdStr);
        log.info("[WF-SUBMISSION] open UI for submissionId={}", submissionId);

        FormSubmissionDTO submission = submissionService.getById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));
        WorkflowDTO workflow = workflowService.getById(submission.workflowId())
                .orElseThrow(() -> new NoSuchElementException("Workflow not found: " + submission.workflowId()));

        FormDefinitionEntity formDef = formDefRepo
                .findFirstByKeyAndVersion(submission.formKey(), submission.formVersion())
                .orElseThrow(() -> new NoSuchElementException(
                        "FormDefinition not found: " + submission.formKey() + " v" + submission.formVersion()));

        boolean readonly = submission.state() == CMSState.COMPLETED;

        List<StepView> steps = List.of();
        ModuleView module = new ModuleView(
                Optional.ofNullable(formDef.getFormName()).orElse(""),
                Optional.ofNullable(formDef.getFormDescription()).orElse(""),
                formDef.getFormDefinitionId() == null ? "" : formDef.getFormDefinitionId().toString()
        );

        List<ComponentView> components = componentBuilder.buildComponents(
                formDef.getJsonSchema(),
                formDef.getUiSchema(),
                Optional.ofNullable(submission.payload()).orElse(Map.of())
        );

        log.debug("[WF-SUBMISSION] built {} components for submissionId={}, readonly={}",
                components.size(), submissionId, readonly);

        model.addAttribute("steps", steps);
        model.addAttribute("module", module);
        model.addAttribute("workflow", workflow);
        model.addAttribute("components", components);
        model.addAttribute("submissionId", submission.id().toString());
        model.addAttribute("readonly", readonly);
        model.addAttribute("checksum", submission.checksum());

        return "workflow/interface/workflowSubmission";
    }

    @PostMapping(path = "/workflow/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAndSave(
            @RequestParam MultiValueMap<String, String> params,
            @RequestParam(required = false) Map<String, MultipartFile> fileMap
    ) {
        long t0 = System.currentTimeMillis();
        try {
            String idStr = params.getFirst("id");
            if (!StringUtils.hasText(idStr)) {
                log.warn("[WF-UPLOAD] missing submission id");
                return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Missing submission id"));
            }
            UUID submissionId = UUID.fromString(idStr);

            FormSubmissionDTO existing = submissionService.getById(submissionId)
                    .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

            if (existing.state() == CMSState.COMPLETED) {
                log.info("[WF-UPLOAD] Submission already COMPLETED; idempotent return. submissionId={}", submissionId);
                return ResponseEntity.ok(Map.of("ok", true, "redirectUrl", "/workflow/submission?id=" + submissionId));
            }

            log.info("[WF-UPLOAD] start merge payload for submissionId={}, files={}", submissionId,
                    fileMap == null ? 0 : fileMap.size());

            Map<String, Object> mergedPayload = payloadService.mergeParamsAndFiles(
                    Optional.ofNullable(existing.payload()).orElse(Map.of()),
                    params,
                    fileMap
            );
            String checksum = payloadService.computeChecksum(mergedPayload);

            FormSubmissionDTO updated = new FormSubmissionDTO(
                    existing.id(),
                    existing.workflowId(),
                    existing.formKey(),
                    existing.formVersion(),
                    CMSState.COMPLETED,
                    existing.submittedBy(),
                    CMSDate.of(System.currentTimeMillis()),
                    checksum,
                    mergedPayload
            );

            submissionService.save(updated);

            // AuditTrail + Log
            auditTrailService.logSimple(
                    existing.workflowId(),
                    existing.submittedBy(),
                    "SUBMISSION_COMPLETED",
                    Map.of("submissionId", existing.id().toString(), "formKey", existing.formKey()),
                    Map.of("checksum", checksum, "payloadSize", mergedPayload.size())
            );
            log.info("[WF-UPLOAD] completed submissionId={} in {}ms, checksum={}, payloadSize={}",
                    submissionId, (System.currentTimeMillis() - t0), checksum, mergedPayload.size());

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "redirectUrl", "/workflow/submission?id=" + submissionId
            ));
        } catch (Exception e) {
            log.error("[WF-UPLOAD] failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        }
    }
}
