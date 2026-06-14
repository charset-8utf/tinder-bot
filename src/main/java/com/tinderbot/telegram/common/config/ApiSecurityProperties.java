package com.tinderbot.telegram.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tinderbot.api.security")
public record ApiSecurityProperties(boolean enabled) {
}
