package com.messdiener.cms.api.sso.config;

import com.messdiener.cms.api.sso.dto.SsoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SsoProperties.class)
public class SsoConfig {}
