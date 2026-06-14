package com.tinderbot.telegram.integration;

import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.repository.UserSessionRepository;
import com.tinderbot.telegram.service.session.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserSessionServiceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserSessionRepository repository;

    private static final Long CHAT_ID = 9_003L;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
        userSessionService.evictMemoryCacheForTests();
    }

    @Test
    void session_persistsAcrossCacheEviction() {
        userSessionService.setCurrentMode(CHAT_ID, DialogMode.DATE);
        userSessionService.setCurrentStarKey(CHAT_ID, "gosling");

        userSessionService.evictMemoryCacheForTests();

        assertThat(userSessionService.getCurrentMode(CHAT_ID)).isEqualTo(DialogMode.DATE);
        assertThat(userSessionService.getCurrentStarKey(CHAT_ID)).isEqualTo("gosling");
        assertThat(repository.existsById(CHAT_ID)).isTrue();
    }

    @Test
    void delete_removesFromDatabaseAndMemory() {
        userSessionService.setCurrentMode(CHAT_ID, DialogMode.MESSAGE);
        userSessionService.delete(CHAT_ID);

        assertThat(repository.existsById(CHAT_ID)).isFalse();
        userSessionService.evictMemoryCacheForTests();
        assertThat(userSessionService.getCurrentMode(CHAT_ID)).isEqualTo(DialogMode.MAIN);
    }
}
