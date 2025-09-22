package com.messdiener.cms.notifications.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnExpression("'${cms.notifications.push:noop}'!='fcm'")
public class NoopPushGateway implements PushGateway {

    private static final Logger log = LoggerFactory.getLogger(NoopPushGateway.class);

    @Override
    public void send(List<String> tokens, String title, String body, Map<String, String> data) {
        log.info("[NOOP] send(tokens={}, title='{}', body='{}', data={})", tokens.size(), title, body, data);
    }

    @Override
    public String providerName() {
        return "noop";
    }
}
