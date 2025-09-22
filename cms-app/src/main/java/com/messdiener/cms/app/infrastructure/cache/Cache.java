package com.messdiener.cms.app.infrastructure.cache;

import com.messdiener.cms.app.infrastructure.scheduler.GlobalManager;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public class Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cache.class);

    // Dienste (werden von Spring injiziert)
    private final GlobalManager globalManager;

    @PostConstruct
    public void init() {
        LOGGER.info("Cache initialized by Spring.");
            LOGGER.info("Post-init setup for person login and normed tasks completed.");
    }
}
