package com.tinderbot.telegram.api;

import com.tinderbot.telegram.core.MultiSessionTelegramBot;

public interface IMessageCleaner {
    void deleteAllMessages(Long chatId, MultiSessionTelegramBot bot);
    void deleteCurrentMenu(Long chatId, MultiSessionTelegramBot bot);
}