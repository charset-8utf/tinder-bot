package com.tinderbot.telegram.service.session;

import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.model.UserSession;
import com.tinderbot.telegram.entity.UserSessionEntity;
import com.tinderbot.telegram.mapper.GptMessageMapperImpl;
import com.tinderbot.telegram.mapper.UserInfoMapperImpl;
import com.tinderbot.telegram.mapper.UserSessionMapperImpl;
import com.tinderbot.telegram.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@EntityScan(basePackageClasses = UserSessionEntity.class)
@EnableJpaRepositories(basePackageClasses = UserSessionRepository.class)
@Import({UserSessionService.class, UserSessionMapperImpl.class, GptMessageMapperImpl.class, UserInfoMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserSessionServiceTest {

    @Autowired
    private UserSessionRepository repository;

    @Autowired
    private UserSessionService service;

    private final long chatId1 = 123L;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        service.evictMemoryCacheForTests();
    }

    @Test
    void getOrCreate_shouldCreateNewSessionWhenNotExists() {
        UserSession session = service.getOrCreate(chatId1);
        assertNotNull(session);
        assertEquals(DialogMode.MAIN, session.getCurrentMode());
        assertNull(session.getPhotoMessageId());
        assertNull(session.getWelcomeMessageId());
        assertNull(session.getCurrentMenuMessageId());
        assertTrue(session.getBotMessageIds().isEmpty());
    }

    @Test
    void getOrCreate_shouldReturnSameSessionForSameChatId() {
        UserSession session1 = service.getOrCreate(chatId1);
        UserSession session2 = service.getOrCreate(chatId1);
        assertSame(session1, session2);
    }

    @Test
    void getOrCreate_shouldCreateDifferentSessionsForDifferentChatIds() {
        long chatId2 = 456L;
        UserSession session1 = service.getOrCreate(chatId1);
        UserSession session2 = service.getOrCreate(chatId2);
        assertNotSame(session1, session2);
    }

    @Test
    void setCurrentMode_shouldUpdateMode() {
        service.setCurrentMode(chatId1, DialogMode.GPT);
        assertEquals(DialogMode.GPT, service.getCurrentMode(chatId1));
    }

    @Test
    void getCurrentMode_shouldReturnMainByDefault() {
        assertEquals(DialogMode.MAIN, service.getCurrentMode(chatId1));
    }

    @Test
    void setPhotoMessageId_shouldStoreAndReturnOptional() {
        service.setPhotoMessageId(chatId1, 100);
        Optional<Integer> photoId = service.getPhotoMessageId(chatId1);
        assertTrue(photoId.isPresent());
        assertEquals(100, photoId.get());
    }

    @Test
    void getPhotoMessageId_shouldReturnEmptyWhenNotSet() {
        Optional<Integer> photoId = service.getPhotoMessageId(chatId1);
        assertFalse(photoId.isPresent());
    }

    @Test
    void setWelcomeMessageId_shouldStoreAndReturnOptional() {
        service.setWelcomeMessageId(chatId1, 101);
        Optional<Integer> welcomeId = service.getWelcomeMessageId(chatId1);
        assertTrue(welcomeId.isPresent());
        assertEquals(101, welcomeId.get());
    }

    @Test
    void getWelcomeMessageId_shouldReturnEmptyWhenNotSet() {
        Optional<Integer> welcomeId = service.getWelcomeMessageId(chatId1);
        assertFalse(welcomeId.isPresent());
    }

    @Test
    void setCurrentMenuMessageId_shouldStoreAndReturnOptional() {
        service.setCurrentMenuMessageId(chatId1, 102);
        Optional<Integer> menuId = service.getCurrentMenuMessageId(chatId1);
        assertTrue(menuId.isPresent());
        assertEquals(102, menuId.get());
    }

    @Test
    void getCurrentMenuMessageId_shouldReturnEmptyWhenNotSet() {
        Optional<Integer> menuId = service.getCurrentMenuMessageId(chatId1);
        assertFalse(menuId.isPresent());
    }

    @Test
    void addBotMessageId_shouldAppendToList() {
        service.addBotMessageId(chatId1, 200);
        service.addBotMessageId(chatId1, 201);
        List<Integer> ids = service.getBotMessageIds(chatId1);
        assertThat(ids).containsExactly(200, 201);
    }

    @Test
    void getBotMessageIds_shouldReturnCopyOfList() {
        service.addBotMessageId(chatId1, 200);
        List<Integer> ids1 = service.getBotMessageIds(chatId1);
        List<Integer> ids2 = service.getBotMessageIds(chatId1);
        assertNotSame(ids1, ids2);
    }

    @Test
    void clearBotMessageIds_shouldRemoveAll() {
        service.addBotMessageId(chatId1, 200);
        service.addBotMessageId(chatId1, 201);
        service.clearBotMessageIds(chatId1);
        assertTrue(service.getBotMessageIds(chatId1).isEmpty());
    }

    @Test
    void clearMessageIds_shouldSetAllIdsToNull() {
        service.setPhotoMessageId(chatId1, 100);
        service.setWelcomeMessageId(chatId1, 101);
        service.setCurrentMenuMessageId(chatId1, 102);
        service.clearMessageIds(chatId1);

        assertAll(
                () -> assertFalse(service.getPhotoMessageId(chatId1).isPresent()),
                () -> assertFalse(service.getWelcomeMessageId(chatId1).isPresent()),
                () -> assertFalse(service.getCurrentMenuMessageId(chatId1).isPresent())
        );
    }

    @Test
    void clearMessageIds_shouldNotAffectBotMessageIds() {
        service.addBotMessageId(chatId1, 200);
        service.clearMessageIds(chatId1);
        assertThat(service.getBotMessageIds(chatId1)).containsExactly(200);
    }

    @Test
    void clearMessageIds_shouldNotCreateSessionWhenNoneExists() {
        service.clearMessageIds(chatId1);
        assertEquals(0, repository.count());
    }

    @Test
    void setCurrentStarKey_shouldStoreAndRetrieve() {
        service.setCurrentStarKey(chatId1, "gosling");
        assertEquals("gosling", service.getCurrentStarKey(chatId1));
    }

    @Test
    void getCurrentStarKey_shouldReturnNullByDefault() {
        assertNull(service.getCurrentStarKey(chatId1));
    }

    @Test
    void addMessageToHistory_shouldAppendToList() {
        service.addMessageToHistory(chatId1, "Hello");
        service.addMessageToHistory(chatId1, "World");
        List<String> history = service.getMessageHistory(chatId1);
        assertThat(history).containsExactly("Hello", "World");
    }

    @Test
    void getMessageHistory_shouldReturnCopyOfList() {
        service.addMessageToHistory(chatId1, "Hello");
        List<String> history1 = service.getMessageHistory(chatId1);
        List<String> history2 = service.getMessageHistory(chatId1);
        assertNotSame(history1, history2);
    }

    @Test
    void clearMessageHistory_shouldRemoveAll() {
        service.addMessageToHistory(chatId1, "Hello");
        service.clearMessageHistory(chatId1);
        assertTrue(service.getMessageHistory(chatId1).isEmpty());
    }

    @Test
    void setProfileStep_shouldStoreAndRetrieve() {
        service.setProfileStep(chatId1, 3);
        assertEquals(3, service.getProfileStep(chatId1));
    }

    @Test
    void getProfileStep_shouldReturnZeroByDefault() {
        assertEquals(0, service.getProfileStep(chatId1));
    }

    @Test
    void setProfileTemp_shouldStoreAndRetrieve() {
        UserInfo info = new UserInfo();
        info.setName("Test");
        service.setProfileTemp(chatId1, info);
        UserInfo retrieved = service.getProfileTemp(chatId1);
        assertSame(info, retrieved);
    }

    @Test
    void getProfileTemp_shouldCreateNewIfNull() {
        UserInfo retrieved = service.getProfileTemp(chatId1);
        assertNotNull(retrieved);
        UserInfo retrieved2 = service.getProfileTemp(chatId1);
        assertSame(retrieved, retrieved2);
    }

    @Test
    void setOpenerStep_shouldStoreAndRetrieve() {
        service.setOpenerStep(chatId1, 2);
        assertEquals(2, service.getOpenerStep(chatId1));
    }

    @Test
    void getOpenerStep_shouldReturnZeroByDefault() {
        assertEquals(0, service.getOpenerStep(chatId1));
    }

    @Test
    void setOpenerTemp_shouldStoreAndRetrieve() {
        UserInfo info = new UserInfo();
        info.setName("Test");
        service.setOpenerTemp(chatId1, info);
        UserInfo retrieved = service.getOpenerTemp(chatId1);
        assertSame(info, retrieved);
    }

    @Test
    void getOpenerTemp_shouldCreateNewIfNull() {
        UserInfo retrieved = service.getOpenerTemp(chatId1);
        assertNotNull(retrieved);
        UserInfo retrieved2 = service.getOpenerTemp(chatId1);
        assertSame(retrieved, retrieved2);
    }

    @Test
    void session_shouldSurviveCacheEvictAndReloadFromDb() {
        service.setCurrentMode(chatId1, DialogMode.GPT);
        service.evictMemoryCacheForTests();
        assertEquals(DialogMode.GPT, service.getCurrentMode(chatId1));
    }
}
