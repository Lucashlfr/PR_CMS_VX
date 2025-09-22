package com.messdiener.cms.notifications.api.config;

// src/main/java/com/messdiener/cms/v3/api/notifications/config/WebSocketConfig.java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // optional
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        var c = new ServletServerContainerFactoryBean();
        c.setMaxTextMessageBufferSize(256 * 1024);
        c.setMaxBinaryMessageBufferSize(256 * 1024);
        return c;
    }
}
