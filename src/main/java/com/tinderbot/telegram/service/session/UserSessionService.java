package com.tinderbot.telegram.service.session;

import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.model.UserSession;
import com.tinderbot.telegram.mapper.UserSessionMapper;
import com.tinderbot.telegram.repository.UserSessionRepository;
import com.plexpt.chatgpt.entity.chat.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserSessionService implements ISessionService {

    private final SessionPersistence persistence;

    public UserSessionService(UserSessionRepository repository, UserSessionMapper sessionMapper) {
        this.persistence = new SessionPersistence(repository, sessionMapper);
    }

    public void evictMemoryCacheForTests() {
        persistence.evictMemoryCache();
    }

    @Override
    public UserSession getOrCreate(Long chatId) {
        return persistence.getOrCreate(chatId);
    }

    @Override
    public DialogMode getCurrentMode(Long chatId) {
        return persistence.read(chatId, UserSession::getCurrentMode);
    }

    @Override
    public void setCurrentMode(Long chatId, DialogMode mode) {
        persistence.write(chatId, session -> session.setCurrentMode(mode));
    }

    @Override
    public Optional<Integer> getPhotoMessageId(Long chatId) {
        return Optional.ofNullable(persistence.read(chatId, UserSession::getPhotoMessageId));
    }

    @Override
    public void setPhotoMessageId(Long chatId, Integer messageId) {
        persistence.write(chatId, session -> session.setPhotoMessageId(messageId));
    }

    @Override
    public Optional<Integer> getWelcomeMessageId(Long chatId) {
        return Optional.ofNullable(persistence.read(chatId, UserSession::getWelcomeMessageId));
    }

    @Override
    public void setWelcomeMessageId(Long chatId, Integer messageId) {
        persistence.write(chatId, session -> session.setWelcomeMessageId(messageId));
    }

    @Override
    public Optional<Integer> getCurrentMenuMessageId(Long chatId) {
        return Optional.ofNullable(persistence.read(chatId, UserSession::getCurrentMenuMessageId));
    }

    @Override
    public void setCurrentMenuMessageId(Long chatId, Integer messageId) {
        persistence.write(chatId, session -> session.setCurrentMenuMessageId(messageId));
    }

    @Override
    public void addBotMessageId(Long chatId, Integer messageId) {
        persistence.write(chatId, session -> session.addBotMessageId(messageId));
    }

    @Override
    public List<Integer> getBotMessageIds(Long chatId) {
        return persistence.read(chatId, UserSession::getBotMessageIds);
    }

    @Override
    public void clearBotMessageIds(Long chatId) {
        persistence.write(chatId, UserSession::clearBotMessageIds);
    }

    @Override
    public void clearMessageIds(Long chatId) {
        persistence.mutateIfPresent(chatId, session -> {
            session.setPhotoMessageId(null);
            session.setWelcomeMessageId(null);
            session.setCurrentMenuMessageId(null);
        });
    }

    @Override
    public String getCurrentStarKey(Long chatId) {
        return persistence.read(chatId, UserSession::getCurrentStarKey);
    }

    @Override
    public void setCurrentStarKey(Long chatId, String starKey) {
        persistence.write(chatId, session -> session.setCurrentStarKey(starKey));
    }

    @Override
    public List<String> getMessageHistory(Long chatId) {
        return persistence.read(chatId, UserSession::getMessageHistory);
    }

    @Override
    public void addMessageToHistory(Long chatId, String message) {
        persistence.write(chatId, session -> session.addMessageToHistory(message));
    }

    @Override
    public void clearMessageHistory(Long chatId) {
        persistence.write(chatId, UserSession::clearMessageHistory);
    }

    @Override
    public int getProfileStep(Long chatId) {
        return persistence.read(chatId, UserSession::getProfileStep);
    }

    @Override
    public void setProfileStep(Long chatId, int step) {
        persistence.write(chatId, session -> session.setProfileStep(step));
    }

    @Override
    public UserInfo getProfileTemp(Long chatId) {
        return persistence.read(chatId, session -> Optional.ofNullable(session.getProfileTemp())
                .orElseGet(() -> {
                    UserInfo userInfo = new UserInfo();
                    session.setProfileTemp(userInfo);
                    return userInfo;
                }));
    }

    @Override
    public void setProfileTemp(Long chatId, UserInfo profileTemp) {
        persistence.write(chatId, session -> session.setProfileTemp(profileTemp));
    }

    @Override
    public int getOpenerStep(Long chatId) {
        return persistence.read(chatId, UserSession::getOpenerStep);
    }

    @Override
    public void setOpenerStep(Long chatId, int step) {
        persistence.write(chatId, session -> session.setOpenerStep(step));
    }

    @Override
    public UserInfo getOpenerTemp(Long chatId) {
        return persistence.read(chatId, session -> Optional.ofNullable(session.getOpenerTemp())
                .orElseGet(() -> {
                    UserInfo userInfo = new UserInfo();
                    session.setOpenerTemp(userInfo);
                    return userInfo;
                }));
    }

    @Override
    public void setOpenerTemp(Long chatId, UserInfo openerTemp) {
        persistence.write(chatId, session -> session.setOpenerTemp(openerTemp));
    }

    @Override
    public List<Message> getChatGptHistory(Long chatId) {
        return persistence.read(chatId, UserSession::getChatGptHistory);
    }

    @Override
    public void setChatGptHistory(Long chatId, List<Message> history) {
        persistence.write(chatId, session -> session.setChatGptHistory(history));
    }

    @Override
    public void clearChatGptHistory(Long chatId) {
        persistence.write(chatId, UserSession::clearChatGptHistory);
    }

    @Override
    public int getDateMessageCount(Long chatId) {
        return persistence.read(chatId, UserSession::getDateMessageCount);
    }

    @Override
    public void incrementDateMessageCount(Long chatId) {
        persistence.write(chatId, session -> session.setDateMessageCount(session.getDateMessageCount() + 1));
    }

    @Override
    public void resetDateMessageCount(Long chatId) {
        persistence.write(chatId, session -> session.setDateMessageCount(0));
    }

    @Override
    public void delete(Long chatId) {
        persistence.delete(chatId);
    }
}
