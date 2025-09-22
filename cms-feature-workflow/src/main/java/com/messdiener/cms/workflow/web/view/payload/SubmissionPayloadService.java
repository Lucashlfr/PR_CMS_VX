// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\payload\SubmissionPayloadService.java
package com.messdiener.cms.workflow.web.view.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.workflow.util.PayloadChecksum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SubmissionPayloadService {

    private static final Set<String> TECHNICAL_FIELDS = Set.of("id", "_csrf");

    private final ObjectMapper om = new ObjectMapper();

    public Map<String, Object> mergeParamsAndFiles(
            Map<String, Object> existingPayload,
            MultiValueMap<String, String> params,
            Map<String, MultipartFile> fileMap
    ) {
        Map<String, Object> merged = new LinkedHashMap<>(existingPayload == null ? Map.of() : existingPayload);

        // 1) simple fields
        if (params != null) {
            params.forEach((k, v) -> {
                if (!TECHNICAL_FIELDS.contains(k) && v != null) {
                    if (v.size() == 1) merged.put(k, v.getFirst());
                    else merged.put(k, v);
                }
            });
        }

        // 2) hidden JSON arrays (string -> [ ... ])
        merged.replaceAll((k, v) -> tryParseJsonArray(v));

        // 3) files -> meta
        if (fileMap != null) {
            fileMap.forEach((field, file) -> {
                if (file != null && !file.isEmpty()) {
                    Map<String, Object> fileInfo = new LinkedHashMap<>();
                    fileInfo.put("originalFilename", file.getOriginalFilename());
                    fileInfo.put("size", file.getSize());
                    fileInfo.put("contentType", file.getContentType());
                    merged.put(field, fileInfo);
                }
            });
        }
        return merged;
    }

    public String computeChecksum(Map<String, Object> payload) {
        return PayloadChecksum.compute(payload);
    }

    private Object tryParseJsonArray(Object v) {
        if (!(v instanceof String s)) return v;
        String trimmed = s.trim();
        if (!(trimmed.startsWith("[") && trimmed.endsWith("]"))) return v;
        try {
            return om.readValue(trimmed, List.class);
        } catch (Exception ignore) {
            return v;
        }
    }
}
