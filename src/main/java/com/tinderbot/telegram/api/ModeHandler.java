package com.tinderbot.telegram.api;

import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;

public interface ModeHandler {
    DialogMode getMode();
    void onCommand(MultiSessionTelegramBot bot, Long chatId);
    void onMessage(MultiSessionTelegramBot bot, Long chatId, String text);
}