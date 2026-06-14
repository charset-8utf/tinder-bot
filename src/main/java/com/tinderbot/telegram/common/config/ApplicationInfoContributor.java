package com.tinderbot.telegram.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInfoContributor implements InfoContributor {

    private final String applicationName;
    private final String description;

    public ApplicationInfoContributor(
            @Value("${spring.application.name}") String applicationName,
            @Value("${info.app.description:TinderBot — Telegram-бот для знакомств с REST API}") String description) {
        this.applicationName = applicationName;
        this.description = description;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("application", applicationName);
        builder.withDetail("description", description);
        builder.withDetail("api", "/api/v1");
        builder.withDetail("swaggerUi", "/swagger-ui.html");
    }
}
