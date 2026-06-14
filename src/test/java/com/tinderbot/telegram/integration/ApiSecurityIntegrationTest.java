package com.tinderbot.telegram.integration;

import com.tinderbot.telegram.dto.LoginRequest;
import com.tinderbot.telegram.dto.LoginResponse;
import com.tinderbot.telegram.service.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "tinderbot.api.security.enabled=true")
class ApiSecurityIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void api_withoutJwt_returns401() {
        ResponseEntity<String> response = anonymousClient().getForEntity(
                baseUrl() + "/sessions/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void jwtLogin_thenAccessApi() {
        ResponseEntity<LoginResponse> login = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                new LoginRequest("demo", "password"),
                LoginResponse.class);

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).isNotNull();
        assertThat(login.getBody().accessToken()).isNotBlank();
        assertThat(jwtService.validateAndExtractUsername(login.getBody().accessToken())).contains("demo");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().accessToken());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/sessions/1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void jwtLogin_canAccessLinkedSession_only() {
        ResponseEntity<LoginResponse> login = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                new LoginRequest("demo", "password"),
                LoginResponse.class);

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().accessToken());

        ResponseEntity<String> ownSession = restTemplate.exchange(
                baseUrl() + "/sessions/1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(ownSession.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> foreignSession = restTemplate.exchange(
                baseUrl() + "/sessions/999",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(foreignSession.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void jwtLogin_invalidCredentials_returns401() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                new LoginRequest("demo", "wrong"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Неверное имя пользователя или пароль");
    }
}
