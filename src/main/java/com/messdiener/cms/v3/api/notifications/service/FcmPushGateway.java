package com.messdiener.cms.v3.api.notifications.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.messdiener.cms.v3.api.notifications.config.FcmProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnExpression(
        "'${cms.notifications.push:}'=='fcm' and '${notifications.fcm.enabled:false}'=='true'"
)
public class FcmPushGateway implements PushGateway {

    private static final Logger log = LoggerFactory.getLogger(FcmPushGateway.class);
    private static final String APP_NAME = "cmsx-fcm";
    private static final String ANDROID_CHANNEL_ID = "high_importance";

    private final FcmProperties props;
    private FirebaseApp app;

    public FcmPushGateway(FcmProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() throws IOException {
        // Reuse bei Devtools-Restarts
        for (FirebaseApp existing : FirebaseApp.getApps()) {
            if (APP_NAME.equals(existing.getName())) {
                this.app = existing;
                log.info("[FCM] Reusing existing FirebaseApp '{}'", APP_NAME);
                return;
            }
        }

        if (props.getCredentials() == null) {
            throw new IllegalStateException("notifications.fcm.credentials is not set");
        }

        try (InputStream in = props.getCredentials().getInputStream()) {
            GoogleCredentials creds = GoogleCredentials.fromStream(in);
            FirebaseOptions.Builder builder = FirebaseOptions.builder().setCredentials(creds);
            if (props.getProjectId() != null && !props.getProjectId().isBlank()) {
                builder.setProjectId(props.getProjectId());
            }
            FirebaseOptions options = builder.build();
            this.app = FirebaseApp.initializeApp(options, APP_NAME);
            log.info("[FCM] FirebaseApp '{}' initialized", APP_NAME);
        }
    }

    @PreDestroy
    public void destroy() {
        if (app != null) {
            log.info("[FCM] Deleting FirebaseApp '{}'", app.getName());
            app.delete();
        }
    }

    @Override
    public void send(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            log.debug("[FCM] no tokens -> nothing to send");
            return;
        }

        boolean silent = data != null && "true".equalsIgnoreCase(data.get("silent"));

        MulticastMessage.Builder msg = MulticastMessage.builder().addAllTokens(tokens);

        if (silent) {
            // SILENT / DATA PUSH
            AndroidConfig android = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build();

            ApnsConfig apns = ApnsConfig.builder()
                    .putHeader("apns-priority", "5") // Hintergrund
                    .setAps(Aps.builder().setContentAvailable(true).build())
                    .build();

            msg.setAndroidConfig(android).setApnsConfig(apns);

            if (data != null && !data.isEmpty()) {
                msg.putAllData(data);
            }
        } else {
            // DISPLAY / HEADS-UP PUSH
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            AndroidNotification androidNotification = AndroidNotification.builder()
                    .setChannelId(ANDROID_CHANNEL_ID)
                    .setSound("default")
                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                    .build();

            AndroidConfig android = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(androidNotification)
                    .build();

            Aps aps = Aps.builder()
                    .setSound("default")
                    .build();

            ApnsConfig apns = ApnsConfig.builder()
                    .putHeader("apns-priority", "10") // sofort anzeigen
                    .setAps(aps)
                    .build();

            msg.setNotification(notification)
                    .setAndroidConfig(android)
                    .setApnsConfig(apns);

            if (data != null && !data.isEmpty()) {
                msg.putAllData(data);
            }
        }

        try {
            BatchResponse resp = FirebaseMessaging.getInstance(app).sendEachForMulticast(msg.build());
            log.info("[FCM] multicast sent: success={}, failure={}", resp.getSuccessCount(), resp.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] multicast send failed", e);
        }
    }

    @Override
    public String providerName() {
        return "fcm";
    }
}
