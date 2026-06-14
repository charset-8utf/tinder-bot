package com.tinderbot.telegram.integration;

import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.repository.UserSessionRepository;
import com.tinderbot.telegram.service.session.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRestControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserSessionRepository repository;

    private static final Long CHAT_ID = 9_001L;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
        userSessionService.evictMemoryCacheForTests();
    }

    @Test
    void getSession_createsAndReturnsState() {
        userSessionService.setCurrentMode(CHAT_ID, DialogMode.GPT);

        ResponseEntity<SessionResponse> response = restTemplate.getForEntity(
                baseUrl() + "/sessions/" + CHAT_ID, SessionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().chatId()).isEqualTo(CHAT_ID);
        assertThat(response.getBody().currentMode()).isEqualTo(DialogMode.GPT);
    }

    @Test
    void deleteSession_removesPersistedSession() {
        userSessionService.setCurrentMode(CHAT_ID, DialogMode.PROFILE);
        assertThat(repository.existsById(CHAT_ID)).isTrue();

        restTemplate.delete(baseUrl() + "/sessions/" + CHAT_ID);

        assertThat(repository.existsById(CHAT_ID)).isFalse();
        userSessionService.evictMemoryCacheForTests();
        assertThat(userSessionService.getCurrentMode(CHAT_ID)).isEqualTo(DialogMode.MAIN);
    }

    @Test
    void deleteSession_whenMissing_returns404() {
        var response = restTemplate.exchange(
                baseUrl() + "/sessions/999999",
                org.springframework.http.HttpMethod.DELETE,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
