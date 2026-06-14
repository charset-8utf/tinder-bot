package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.CallbackHandler;
import com.tinderbot.telegram.api.ModeHandler;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseCallbackHandler implements CallbackHandler {
    protected final ModeHandler modeHandler;

    @Override
    public void execute(MultiSessionTelegramBot bot, Long chatId, String callback) {
        modeHandler.onCommand(bot, chatId);
    }
}