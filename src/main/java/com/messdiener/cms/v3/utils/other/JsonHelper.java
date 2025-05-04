package com.messdiener.cms.v3.utils.other;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonHelper {

    public static String buildNameValueJson(Map<String, String> params) {
        try {
            List<Map<String, String>> result = params.entrySet().stream()
                    .map(e -> Map.of("name", e.getKey(), "value", e.getValue()))
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build JSON from params", e);
        }
    }

    public static String getValueFromJson(String jsonInput, String name) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> list = mapper.readValue(jsonInput, new TypeReference<List<Map<String, String>>>() {});

            for (Map<String, String> entry : list) {
                if (name.equals(entry.get("name"))) {
                    return entry.get("value");
                }
            }
            return null; // Falls Name nicht gefunden wird
        } catch (Exception e) {
            throw new RuntimeException("Failed to read value from JSON", e);
        }
    }

    public static Map<String, String> parseJsonToParams(String jsonInput) {
        if (jsonInput == null || jsonInput.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> list = mapper.readValue(jsonInput, new TypeReference<List<Map<String, String>>>() {});

            Map<String, String> params = new HashMap<>();
            for (Map<String, String> entry : list) {
                String entryName = entry.get("name");
                String entryValue = entry.get("value");
                if (entryName != null) {
                    params.put(entryName, entryValue != null ? entryValue : "");
                }
            }
            return params;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to params", e);
        }
    }

    /**
     * Prüft, ob ein gegebener Name in der JSON-Liste vorhanden ist.
     *
     * @param jsonInput JSON-String im Format einer Liste von Objekten mit "name" und "value".
     * @param name Der zu suchende Name.
     * @return true, falls der Name vorhanden ist, sonst false.
     */
    public static boolean hasNameInJson(String jsonInput, String name) {
        if (jsonInput == null || jsonInput.trim().isEmpty() || name == null) {
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> list = mapper.readValue(
                    jsonInput,
                    new TypeReference<List<Map<String, String>>>() {}
            );
            return list.stream().anyMatch(entry -> name.equals(entry.get("name")));
        } catch (Exception e) {
            throw new RuntimeException("Failed to check name in JSON", e);
        }
    }
}
