package com.messdiener.cms.notifications.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PushGateway {

    /**
     * Basismethode für Push. Implementierungen können anhand data["silent"] entscheiden,
     * ob Display- oder Silent-Push gebaut wird.
     */
    void send(List<String> tokens, String title, String body, Map<String, String> data);

    /** Komfort-Helfer für Display-Push. */
    default void sendDisplay(List<String> tokens, String title, String body, Map<String, String> data) {
        Map<String, String> payload = (data == null) ? new HashMap<>() : new HashMap<>(data);
        payload.put("silent", "false");
        send(tokens, title, body, payload);
    }

    /** Komfort-Helfer für Silent/Data-Push. */
    default void sendSilent(List<String> tokens, Map<String, String> data) {
        Map<String, String> payload = (data == null) ? new HashMap<>() : new HashMap<>(data);
        payload.put("silent", "true");
        // Bei Silent-Push sind title/body egal; Implementierung ignoriert sie.
        send(tokens, "", "", payload);
    }

    default String providerName() { return "noop"; }
}
