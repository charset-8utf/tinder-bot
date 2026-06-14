package com.tinderbot.telegram.common.util;

import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class MessageSender {

    private final TelegramUiSessionStore telegramUi;

    /**
     * Отправляет фото и сохраняет его ID в сессии как photoMessageId.
     */
    public Message sendAndSavePhoto(MultiSessionTelegramBot bot, Long chatId, String photoKey) {
        Message photoMsg = bot.sendPhotoMessage(photoKey);
        telegramUi.setPhotoMessageId(chatId, photoMsg.getMessageId());
        return photoMsg;
    }

    /**
     * Отправляет plain-text сообщение и сохраняет его ID в списке botMessageIds.
     */
    public Message sendAndSaveText(MultiSessionTelegramBot bot, Long chatId, String text) {
        Message textMsg = bot.sendTextMessage(text);
        telegramUi.addBotMessageId(chatId, textMsg.getMessageId());
        return textMsg;
    }

    /**
     * Отправляет HTML-сообщение (шаблоны из resources/messages) и сохраняет его ID.
     */
    public Message sendAndSaveHtmlText(MultiSessionTelegramBot bot, Long chatId, String htmlText) {
        Message textMsg = bot.sendHtmlMessage(htmlText);
        telegramUi.addBotMessageId(chatId, textMsg.getMessageId());
        return textMsg;
    }

    /**
     * Отправляет сообщение с инлайн-кнопками и сохраняет его ID в списке botMessageIds.
     */
    public Message sendAndSaveButtons(MultiSessionTelegramBot bot, Long chatId, String text, String... buttons) {
        Message buttonsMsg = bot.sendTextButtonsMessage(text, buttons);
        telegramUi.addBotMessageId(chatId, buttonsMsg.getMessageId());
        return buttonsMsg;
    }

    /**
     * Отправляет plain-text меню и сохраняет его ID как currentMenuMessageId.
     */
    public Message sendAndSaveMenu(MultiSessionTelegramBot bot, Long chatId, String text, String... buttons) {
        Message menuMsg = bot.sendTextButtonsMessage(text, buttons);
        telegramUi.setCurrentMenuMessageId(chatId, menuMsg.getMessageId());
        return menuMsg;
    }

    /**
     * Отправляет HTML-меню и сохраняет его ID как currentMenuMessageId.
     */
    public Message sendAndSaveHtmlMenu(MultiSessionTelegramBot bot, Long chatId, String htmlText, String... buttons) {
        Message menuMsg = bot.sendHtmlButtonsMessage(htmlText, buttons);
        telegramUi.setCurrentMenuMessageId(chatId, menuMsg.getMessageId());
        return menuMsg;
    }
}
