// src/main/java/com/messdiener/cms/v3/api/notifications/controller/NotificationController.java
package com.messdiener.cms.v3.api.notifications.controller;

import com.messdiener.cms.v3.api.notifications.dto.NotificationDto;
import com.messdiener.cms.v3.api.notifications.dto.RegisterDeviceTokenRequest;
import com.messdiener.cms.v3.api.notifications.dto.SendNotificationRequest;
import com.messdiener.cms.v3.api.notifications.service.NotificationService;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityHelper securityHelper;

    private UUID currentUserId() {
        return securityHelper.getPerson()
                .map(Person::getId)
                .orElseThrow(() -> new IllegalStateException("no user"));
    }

    @PostMapping(value = "/register-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerToken(@RequestBody RegisterDeviceTokenRequest req) {
        notificationService.registerDeviceToken(currentUserId(), req.token(), req.platform());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<NotificationDto> list = notificationService.listLatest(currentUserId());
        return ResponseEntity.ok(Map.of("data", list));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable("id") UUID id) {
        notificationService.markRead(currentUserId(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // Test-Endpoint, um eine Notification manuell zu verschicken
    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> send(@RequestBody SendNotificationRequest req) {
        var dto = notificationService.sendToUser(req.userId(), req.title(), req.body(), req.dataJson());
        return ResponseEntity.ok(Map.of("data", dto));
    }

    // NotificationController.java
    @PostMapping(value = "/send-to-current", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sendToCurrent(@RequestBody Map<String, String> body) {
        var dto = notificationService.sendToUser(
                currentUserId(),
                body.getOrDefault("title",""),
                body.getOrDefault("body",""),
                body.getOrDefault("dataJson", null)
        );
        return ResponseEntity.ok(Map.of("data", dto));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {

        notificationService.sendToUser(
                UUID.fromString("f685b17f-0533-466f-82d0-343aa69169e4"),
                "Backend Ping",
                "Das kommt direkt aus dem Service",
                "{\"key\":\"value\"}"
        );


        return ResponseEntity.ok(Map.of("ok", true));
    }

}
