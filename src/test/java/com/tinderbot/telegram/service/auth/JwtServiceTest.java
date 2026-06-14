package com.tinderbot.telegram.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                JsonMapper.builder().build(),
                "demo-only-jwt-secret-change-me-in-real-projects-32-plus-chars",
                60);
    }

    @Test
    void createAndValidateToken() {
        String token = jwtService.createAccessToken("demo");
        assertThat(jwtService.validateAndExtractUsername(token)).contains("demo");
    }
}
