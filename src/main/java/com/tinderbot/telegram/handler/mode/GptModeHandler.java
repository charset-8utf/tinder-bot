package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.dialog.GptDialogService;
import com.tinderbot.telegram.model.TextGenerationResult;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class GptModeHandler implements com.tinderbot.telegram.api.ModeHandler {

    private final IMessageCleaner messageCleaner;
    private final MessageView messageView;
    private final KeyboardFactory keyboardFactory;
    private final MessageSender messageSender;
    private final TelegramUiSessionStore telegramUi;
    private final GptDialogService gptDialogService;

    @Override
    public DialogMode getMode() {
        return DialogMode.GPT;
    }

    @Override
    public void onCommand(MultiSessionTelegramBot bot, Long chatId) {
        messageCleaner.deleteAllMessages(chatId, bot);
        gptDialogService.enterGptMode(chatId);

        messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_GPT);
        messageSender.sendAndSaveMenu(bot, chatId, messageView.getGptMessage(), keyboardFactory.createBackToMainMenuButton());
    }

    @Override
    public void onMessage(MultiSessionTelegramBot bot, Long chatId, String text) {
        if (bot.isMessageCommand()) {
            return;
        }

        Message thinkingMsg = bot.sendTextMessage("ChatGPT думает...");
        telegramUi.addBotMessageId(chatId, thinkingMsg.getMessageId());

        gptDialogService.ask(chatId, text)
                .thenAccept(result -> applyResult(bot, thinkingMsg, result))
                .exceptionally(ex -> {
                    log.error("Ошибка при вызове ChatGPT", ex);
                    bot.updateTextMessage(thinkingMsg, "Извините, произошла ошибка.");
                    return null;
                });
    }

    private void applyResult(MultiSessionTelegramBot bot, Message thinkingMsg, TextGenerationResult result) {
        if (result.failed()) {
            bot.updateTextMessage(thinkingMsg, "Извините, произошла ошибка.");
        } else {
            bot.updateTextMessage(thinkingMsg, result.text().orElse(""));
        }
    }
}
