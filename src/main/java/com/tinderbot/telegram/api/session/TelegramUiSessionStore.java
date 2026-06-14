package com.tinderbot.telegram.api.session;

import java.util.List;
import java.util.Optional;

public interface TelegramUiSessionStore {

    Optional<Integer> getPhotoMessageId(Long chatId);

    void setPhotoMessageId(Long chatId, Integer messageId);

    Optional<Integer> getWelcomeMessageId(Long chatId);

    void setWelcomeMessageId(Long chatId, Integer messageId);

    Optional<Integer> getCurrentMenuMessageId(Long chatId);

    void setCurrentMenuMessageId(Long chatId, Integer messageId);

    void addBotMessageId(Long chatId, Integer messageId);

    List<Integer> getBotMessageIds(Long chatId);

    void clearBotMessageIds(Long chatId);

    void clearMessageIds(Long chatId);
}
