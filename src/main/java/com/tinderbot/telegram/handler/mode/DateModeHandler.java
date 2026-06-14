package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.MenuOption;
import com.tinderbot.telegram.service.dialog.DateDialogService;
import com.tinderbot.telegram.model.DateChatResult;
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
public class DateModeHandler implements com.tinderbot.telegram.api.ModeHandler {

    private final IMessageCleaner messageCleaner;
    private final MessageView messageView;
    private final KeyboardFactory keyboardFactory;
    private final MessageSender messageSender;
    private final TelegramUiSessionStore telegramUi;
    private final DateDialogService dateDialogService;

    @Override
    public DialogMode getMode() {
        return DialogMode.DATE;
    }

    @Override
    public void onCommand(MultiSessionTelegramBot bot, Long chatId) {
        messageCleaner.deleteAllMessages(chatId, bot);
        dateDialogService.enterDateMode(chatId);

        messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_DATE);
        messageSender.sendAndSaveText(bot, chatId, messageView.getDateListMessage());
        messageSender.sendAndSaveButtons(bot, chatId, "Список доступных профилей:", keyboardFactory.createStarButtons());
        messageSender.sendAndSaveButtons(bot, chatId, "...или можно вернуться в главное меню.", "Главное меню", MenuOption.START.getCallback());
    }

    @Override
    public void onMessage(MultiSessionTelegramBot bot, Long chatId, String text) {
        if (bot.isMessageCommand()) {
            return;
        }

        DateChatResult result = dateDialogService.reply(chatId, text);
        if (result.limitExceeded()) {
            bot.sendTextMessage("Лимит сообщений исчерпан. Выберите новую звезду или вернитесь в меню.");
            return;
        }

        Message thinkingMsg = bot.sendTextMessage(result.typingMessage());
        telegramUi.addBotMessageId(chatId, thinkingMsg.getMessageId());

        TextGenerationResult reply = result.reply();
        if (reply.failed()) {
            bot.updateTextMessage(thinkingMsg, "Извините, произошла ошибка.");
        } else {
            bot.updateTextMessage(thinkingMsg, reply.text().orElse(""));
        }
    }
}
