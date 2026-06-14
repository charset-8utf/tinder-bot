package com.tinderbot.telegram.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT_SCHEME = "BearerJwt";

    @Bean
    public OpenAPI tinderBoltOpenApi(
            @Value("${tinderbot.api.security.enabled:false}") boolean securityEnabled) {
        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title("TinderBot API")
                        .description("""
                                REST API для управления сессиями, опросниками и диалогами Telegram-бота.
                                
                                Auth:
                                - JWT: POST /api/v1/auth/login (demo / password) → Authorization: Bearer <token>
                                
                                Группы:
                                - Auth — JWT login
                                - Sessions — состояние сессии и режим диалога
                                - Questionnaires — PROFILE и OPENER
                                - Dialogs — GPT, переписка, DATE (свидание со звездой)
                                """)
                        .version("v1"));

        if (securityEnabled) {
            openApi.components(new Components()
                            .addSecuritySchemes(BEARER_JWT_SCHEME, new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .description("JWT из POST /api/v1/auth/login (demo/password)")))
                    .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT_SCHEME));
        }
        return openApi;
    }
}
