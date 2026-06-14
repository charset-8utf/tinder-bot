package com.tinderbot.telegram.api.session;

import com.plexpt.chatgpt.entity.chat.Message;

import java.util.List;

public interface GptHistoryStore {

    List<Message> getChatGptHistory(Long chatId);

    void setChatGptHistory(Long chatId, List<Message> history);

    void clearChatGptHistory(Long chatId);
}
