package com.tinderbot.telegram.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(
        String name,
        String token,
        @DefaultValue("true") boolean register
) {
}
