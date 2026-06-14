package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.CallbackHandler;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.service.dialog.MessageDialogService;
import com.tinderbot.telegram.model.TextGenerationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class NextMessageCallbackHandler implements CallbackHandler {

    private final TelegramUiSessionStore telegramUi;
    private final MessageDialogService messageDialogService;

    @Override
    public boolean supports(String callback) {
        return CallbackConstants.NEXT_MESSAGE.equals(callback);
    }

    @Override
    public void execute(MultiSessionTelegramBot bot, Long chatId, String callback) {
        TextGenerationResult result = messageDialogService.generateNextMessage(chatId);
        if (result.hasEmptyHistory()) {
            bot.sendTextMessage("Сначала отправьте вашу переписку.");
            return;
        }

        Message thinkingMsg = bot.sendTextMessage("ChatGPT думает над следующим сообщением...");
        telegramUi.addBotMessageId(chatId, thinkingMsg.getMessageId());
        applyResult(bot, thinkingMsg, result);
    }

    private void applyResult(MultiSessionTelegramBot bot, Message thinkingMsg, TextGenerationResult result) {
        if (result.failed()) {
            bot.updateTextMessage(thinkingMsg, "Извините, произошла ошибка.");
        } else {
            bot.updateTextMessage(thinkingMsg, result.text().orElse(""));
        }
    }
}
