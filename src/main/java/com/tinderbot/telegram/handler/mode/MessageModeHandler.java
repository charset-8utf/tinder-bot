package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.ModeHandler;
import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.MenuOption;
import com.tinderbot.telegram.service.dialog.MessageDialogService;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageModeHandler implements ModeHandler {

    private final IMessageCleaner messageCleaner;
    private final MessageView messageView;
    private final MessageSender messageSender;
    private final MessageDialogService messageDialogService;

    @Override
    public DialogMode getMode() {
        return DialogMode.MESSAGE;
    }

    @Override
    public void onCommand(MultiSessionTelegramBot bot, Long chatId) {
        messageCleaner.deleteAllMessages(chatId, bot);
        messageDialogService.enterMessageMode(chatId);
        messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_MESSAGE);
        messageSender.sendAndSaveText(bot, chatId, messageView.getMessageIntro());

        String[] buttons = {
                "Пригласить на свидание", CallbackConstants.INVITE,
                "Следующее сообщение", CallbackConstants.NEXT_MESSAGE,
                "Главное меню", MenuOption.START.getCallback()
        };
        messageSender.sendAndSaveButtons(bot, chatId, "Выберите действие:", buttons);
    }

    @Override
    public void onMessage(MultiSessionTelegramBot bot, Long chatId, String text) {
        if (bot.isMessageCommand()) {
            return;
        }
        if (text == null || text.isBlank()) {
            log.debug("Пустое сообщение от пользователя {}", chatId);
            return;
        }
        messageDialogService.appendUserMessage(chatId, text);
    }
}
