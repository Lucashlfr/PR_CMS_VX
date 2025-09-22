package com.messdiener.cms.workflow.persistence.map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public final class Jsons {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Jsons() {
    }

    public static String toJson(Map<String, Object> map) {
        try {
            return map == null ? "{}" : MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalStateException("JSON serialize failed", e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return json == null || json.isBlank()
                    ? Collections.emptyMap()
                    : MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("JSON parse failed", e);
        }
    }
}
