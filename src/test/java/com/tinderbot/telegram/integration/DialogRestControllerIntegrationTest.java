package com.tinderbot.telegram.integration;

import com.tinderbot.telegram.dto.AppendMessageRequest;
import com.tinderbot.telegram.dto.GptMessageRequest;
import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.dto.TextGenerationResponse;
import com.tinderbot.telegram.dto.UpdateSessionModeRequest;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.repository.UserSessionRepository;
import com.tinderbot.telegram.service.session.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DialogRestControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Long CHAT_ID = 9_003L;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserSessionRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
        userSessionService.evictMemoryCacheForTests();
    }

    @Test
    void updateMode_changesSessionMode() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/sessions/" + CHAT_ID + "/mode",
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateSessionModeRequest(DialogMode.GPT)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<SessionResponse> session = restTemplate.getForEntity(
                baseUrl() + "/sessions/" + CHAT_ID, SessionResponse.class);
        assertThat(session.getBody()).isNotNull();
        assertThat(session.getBody().currentMode()).isEqualTo(DialogMode.GPT);
    }

    @Test
    void askGpt_returnsGeneratedText() {
        ResponseEntity<TextGenerationResponse> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/gpt/messages",
                new HttpEntity<>(new GptMessageRequest("How to start a conversation?")),
                TextGenerationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TextGenerationResponse.Status.GENERATED);
        assertThat(response.getBody().generatedText()).isEqualTo(StubChatGptTestConfiguration.STUB_GENERATED_TEXT);
    }

    @Test
    void generateNextMessage_withoutHistory_returnsEmptyHistoryStatus() {
        ResponseEntity<TextGenerationResponse> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/messages/next",
                null,
                TextGenerationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TextGenerationResponse.Status.GENERATION_EMPTY_HISTORY);
    }

    @Test
    void generateNextMessage_withHistory_returnsGeneratedText() {
        restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/messages",
                new HttpEntity<>(new AppendMessageRequest("Hi, how are you?")),
                Void.class);
        restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/messages",
                new HttpEntity<>(new AppendMessageRequest("I'm good, thanks!")),
                Void.class);

        ResponseEntity<TextGenerationResponse> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/messages/next",
                null,
                TextGenerationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TextGenerationResponse.Status.GENERATED);
        assertThat(response.getBody().generatedText()).isEqualTo(StubChatGptTestConfiguration.STUB_GENERATED_TEXT);
    }

    @Test
    void sendDateMessage_withStarKey_returnsReply() {
        ResponseEntity<com.tinderbot.telegram.dto.DateChatResponse> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/date/messages",
                new HttpEntity<>(new com.tinderbot.telegram.dto.DateMessageRequest(
                        "Привет! Как насчёт кофе?", "date_grande")),
                com.tinderbot.telegram.dto.DateChatResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status())
                .isEqualTo(com.tinderbot.telegram.dto.DateChatResponse.Status.REPLIED);
        assertThat(response.getBody().reply()).isEqualTo(StubChatGptTestConfiguration.STUB_GENERATED_TEXT);
        assertThat(response.getBody().messagesUsed()).isEqualTo(1);
        assertThat(response.getBody().messagesLimit()).isEqualTo(5);
    }

    @Test
    void sendDateMessage_withoutStarKeyOnFirstMessage_returns400() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/date/messages",
                new HttpEntity<>(new com.tinderbot.telegram.dto.DateMessageRequest(
                        "Привет!", null)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("starKey");
    }
}
