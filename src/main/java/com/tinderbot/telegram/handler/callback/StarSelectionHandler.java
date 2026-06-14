package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.CallbackHandler;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.MenuOption;
import com.tinderbot.telegram.service.dialog.DateDialogService;
import com.tinderbot.telegram.model.StarDialogStart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class StarSelectionHandler implements CallbackHandler {

    private final IMessageCleaner messageCleaner;
    private final TelegramUiSessionStore telegramUi;
    private final DateDialogService dateDialogService;

    @Override
    public boolean supports(String callback) {
        return callback.startsWith(CallbackConstants.STAR_PREFIX);
    }

    @Override
    public void execute(MultiSessionTelegramBot bot, Long chatId, String callback) {
        String starKey = callback.replace(CallbackConstants.STAR_PREFIX, "");
        dateDialogService.startStarDialog(chatId, starKey)
                .ifPresentOrElse(
                        start -> renderStarDialog(bot, chatId, start),
                        () -> bot.sendTextMessage("Неизвестная звезда")
                );
    }

    private void renderStarDialog(MultiSessionTelegramBot bot, Long chatId, StarDialogStart start) {
        try {
            messageCleaner.deleteAllMessages(chatId, bot);

            Message photoMsg = bot.sendPhotoMessage(start.photoKey());
            telegramUi.setPhotoMessageId(chatId, photoMsg.getMessageId());

            Message instructionMsg = bot.sendTextMessage(start.instruction());
            telegramUi.addBotMessageId(chatId, instructionMsg.getMessageId());

            Message backButtonsMsg = bot.sendTextButtonsMessage(
                    "Выберите действие:",
                    "← Назад к выбору звезды", MenuOption.DATE.getCallback(),
                    "Главное меню", MenuOption.START.getCallback()
            );
            telegramUi.addBotMessageId(chatId, backButtonsMsg.getMessageId());
        } catch (Exception e) {
            log.error("Ошибка при выборе звезды: {}", start.starKey(), e);
            bot.sendTextMessage("Не удалось начать диалог со звездой. Попробуйте еще раз.");
        }
    }
}
