package com.tinderbot.telegram.api.session;

import java.util.List;

public interface MessageHistoryStore {

    List<String> getMessageHistory(Long chatId);

    void addMessageToHistory(Long chatId, String message);

    void clearMessageHistory(Long chatId);
}
