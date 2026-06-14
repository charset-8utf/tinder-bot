package com.tinderbot.telegram.integration;

import com.tinderbot.telegram.dto.QuestionnaireAnswerRequest;
import com.tinderbot.telegram.dto.QuestionnaireProgressResponse;
import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.repository.UserSessionRepository;
import com.tinderbot.telegram.service.session.UserSessionService;
import com.tinderbot.telegram.model.QuestionnaireType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionnaireRestControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String[] PROFILE_ANSWERS = {
            "28", "Engineer", "Hiking", "Rudeness", "Serious relationship"
    };

    private static final String[] OPENER_ANSWERS = {
            "Anna", "25", "Yoga", "Designer", "Coffee"
    };

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserSessionRepository repository;

    private static final Long CHAT_ID = 9_002L;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
        userSessionService.evictMemoryCacheForTests();
    }

    @Test
    void profileQuestionnaire_startAndAnswerFirstQuestion() {
        ResponseEntity<QuestionnaireProgressResponse> started = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/PROFILE/start",
                null,
                QuestionnaireProgressResponse.class);

        assertThat(started.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(started.getBody()).isNotNull();
        assertThat(started.getBody().status()).isEqualTo(QuestionnaireProgressResponse.Status.STARTED);
        assertThat(started.getBody().question()).isEqualTo("Сколько вам лет?");

        ResponseEntity<QuestionnaireProgressResponse> next = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/PROFILE/answers",
                new HttpEntity<>(new QuestionnaireAnswerRequest("30")),
                QuestionnaireProgressResponse.class);

        assertThat(next.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(next.getBody()).isNotNull();
        assertThat(next.getBody().status()).isEqualTo(QuestionnaireProgressResponse.Status.NEXT_QUESTION);
        assertThat(next.getBody().question()).isEqualTo("Кем вы работаете?");
    }

    @Test
    void profileQuestionnaire_fullCycleAndGenerate() {
        assertThat(startQuestionnaire(QuestionnaireType.PROFILE).status())
                .isEqualTo(QuestionnaireProgressResponse.Status.STARTED);

        QuestionnaireProgressResponse lastAnswer = null;
        for (String answer : PROFILE_ANSWERS) {
            lastAnswer = submitAnswer(QuestionnaireType.PROFILE, answer);
        }

        assertThat(lastAnswer).isNotNull();
        assertThat(lastAnswer.status()).isEqualTo(QuestionnaireProgressResponse.Status.COMPLETED);

        ResponseEntity<QuestionnaireProgressResponse> generated = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/PROFILE/generate",
                null,
                QuestionnaireProgressResponse.class);

        assertThat(generated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(generated.getBody()).isNotNull();
        assertThat(generated.getBody().status()).isEqualTo(QuestionnaireProgressResponse.Status.GENERATED);
        assertThat(generated.getBody().generatedText()).isEqualTo(StubChatGptTestConfiguration.STUB_GENERATED_TEXT);

        ResponseEntity<SessionResponse> session = restTemplate.getForEntity(
                baseUrl() + "/sessions/" + CHAT_ID, SessionResponse.class);
        assertThat(session.getBody()).isNotNull();
        assertThat(session.getBody().currentMode()).isEqualTo(DialogMode.PROFILE);
    }

    @Test
    void openerQuestionnaire_fullCycleAndGenerate() {
        assertThat(startQuestionnaire(QuestionnaireType.OPENER).status())
                .isEqualTo(QuestionnaireProgressResponse.Status.STARTED);

        QuestionnaireProgressResponse lastAnswer = null;
        for (String answer : OPENER_ANSWERS) {
            lastAnswer = submitAnswer(QuestionnaireType.OPENER, answer);
        }

        assertThat(lastAnswer).isNotNull();
        assertThat(lastAnswer.status()).isEqualTo(QuestionnaireProgressResponse.Status.COMPLETED);

        ResponseEntity<QuestionnaireProgressResponse> generated = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/OPENER/generate",
                null,
                QuestionnaireProgressResponse.class);

        assertThat(generated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(generated.getBody()).isNotNull();
        assertThat(generated.getBody().status()).isEqualTo(QuestionnaireProgressResponse.Status.GENERATED);
    }

    @Test
    void generate_withoutCompletedQuestionnaire_returns409() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/PROFILE/generate",
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("не заполнен");
    }

    @Test
    void submitBlankAnswer_returns400() {
        startQuestionnaire(QuestionnaireType.PROFILE);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/PROFILE/answers",
                new HttpEntity<>(new QuestionnaireAnswerRequest("   ")),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private QuestionnaireProgressResponse startQuestionnaire(QuestionnaireType type) {
        ResponseEntity<QuestionnaireProgressResponse> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/" + type + "/start",
                null,
                QuestionnaireProgressResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private QuestionnaireProgressResponse submitAnswer(QuestionnaireType type, String answer) {
        ResponseEntity<QuestionnaireProgressResponse> response = restTemplate.postForEntity(
                baseUrl() + "/sessions/" + CHAT_ID + "/questionnaires/" + type + "/answers",
                new HttpEntity<>(new QuestionnaireAnswerRequest(answer)),
                QuestionnaireProgressResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}
