package com.tinderbot.telegram.api;

import com.tinderbot.telegram.core.MultiSessionTelegramBot;

public interface CallbackHandler {
    boolean supports(String callback);
    void execute(MultiSessionTelegramBot bot, Long chatId, String callback);
}