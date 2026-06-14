package com.tinderbot.telegram.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationEndpointsIntegrationTest extends AbstractPostgresIntegrationTest {

    @Test
    void readiness_isUp() {
        ResponseEntity<String> response = anonymousClient().getForEntity(
                serverRootUrl() + "/actuator/health/readiness",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void info_isAvailable() {
        ResponseEntity<String> response = anonymousClient().getForEntity(
                serverRootUrl() + "/actuator/info",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("application");
    }

    @Test
    void swaggerUi_isAvailable() {
        ResponseEntity<String> response = anonymousClient().exchange(
                serverRootUrl() + "/swagger-ui.html",
                HttpMethod.GET,
                null,
                String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FOUND, HttpStatus.MOVED_PERMANENTLY);
    }

    @Test
    void swaggerUiIndex_isRenderedAsHtml() {
        ResponseEntity<String> response = anonymousClient().exchange(
                serverRootUrl() + "/swagger-ui/index.html",
                HttpMethod.GET,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("text/html");
        assertThat(response.getBody()).contains("swagger-ui");
    }

    @Test
    void openApiJson_containsApiPaths() {
        ResponseEntity<String> response = anonymousClient().getForEntity(
                serverRootUrl() + "/v3/api-docs",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"paths\"");
        assertThat(response.getBody()).contains("/api/v1/auth/login");
        assertThat(response.getBody()).contains("/api/v1/sessions/{chatId}");
    }
}
