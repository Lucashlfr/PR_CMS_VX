// src/main/java/com/messdiener/cms/workflow/util/PayloadChecksum.java
package com.messdiener.cms.workflow.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PayloadChecksum {

    private static final ObjectMapper OM = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    private PayloadChecksum() {}

    /** Stabile SHA-256 über kanonisches JSON (Keys sortiert). */
    public static String compute(Map<String, Object> payload) {
        try {
            // defensiv: Map in LinkedHashMap kopieren (damit Jackson die Sortierung konsistent schreibt)
            Map<String, Object> canonical = deepCopy(payload);
            String json = OM.writeValueAsString(canonical);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<String, Object> src) {
        Map<String, Object> dst = new LinkedHashMap<>();
        if (src == null) return dst;
        for (Map.Entry<String, Object> e : src.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map) {
                dst.put(e.getKey(), deepCopy((Map<String, Object>) v));
            } else if (v instanceof List) {
                dst.put(e.getKey(), deepCopyList((List<Object>) v));
            } else if (v instanceof Iterable) {
                // Kein iterable.stream(): in List kopieren
                dst.put(e.getKey(), deepCopyList(new java.util.ArrayList<Object>() {{
                    for (Object o : (Iterable<?>) v) add(o);
                }}));
            } else {
                dst.put(e.getKey(), v);
            }
        }
        return dst;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> deepCopyList(List<Object> src) {
        java.util.ArrayList<Object> out = new java.util.ArrayList<>();
        if (src == null) return out;
        for (Object v : src) {
            if (v instanceof Map) {
                out.add(deepCopy((Map<String, Object>) v));
            } else if (v instanceof List) {
                out.add(deepCopyList((List<Object>) v));
            } else if (v instanceof Iterable) {
                java.util.ArrayList<Object> tmp = new java.util.ArrayList<>();
                for (Object o : (Iterable<?>) v) tmp.add(o);
                out.add(deepCopyList(tmp));
            } else {
                out.add(v);
            }
        }
        return out;
    }
}
