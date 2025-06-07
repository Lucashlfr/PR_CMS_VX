package com.messdiener.cms.v3.shared.cache;

import com.messdiener.cms.v3.app.configuration.AppConfiguration;
import com.messdiener.cms.v3.shared.scheduler.GlobalManager;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Getter
@Service
@RequiredArgsConstructor
public class Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    // Konstante Konfigurationswerte
    public static final UUID SYSTEM_TENANT = UUID.fromString("d4c5b381-8022-43bf-a67e-5b44562bb94f");
    public static final UUID SYSTEM_USER = UUID.fromString("93dacda6-b951-413a-96dc-9a37858abe3e");
    public static final UUID WEBSITE = UUID.fromString("d4c5b381-8022-43bf-a67e-5b44562bb94f");

    private final AppConfiguration appConfiguration;
    // Dienste (werden von Spring injiziert)
    private final GlobalManager globalManager;

    @PostConstruct
    public void init() {
        LOGGER.info("Cache initialized by Spring.");
            LOGGER.info("Post-init setup for person login and normed tasks completed.");
    }
}
