package com.tinderbot.telegram.core;

import com.tinderbot.telegram.exception.TelegramBotApiException;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MultiSessionTelegramBot extends TelegramLongPollingBot {

    private static final String PARSE_MODE_HTML = "HTML";

    private final BotResourceLoader resourceLoader;
    private final TelegramTextTruncator textTruncator;
    private final String name;

    private final ThreadLocal<Update> updateEvent = new ThreadLocal<>();

    public MultiSessionTelegramBot(String name, String token,
                                   BotResourceLoader resourceLoader,
                                   TelegramTextTruncator textTruncator) {
        super(token);
        this.name = name;
        this.resourceLoader = resourceLoader;
        this.textTruncator = textTruncator;
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public final void onUpdateReceived(Update update) {
        updateEvent.set(update);
        try {
            onUpdateEventReceived(update);
        } finally {
            updateEvent.remove();
        }
    }

    public void onUpdateEventReceived(Update update) {
        // override in subclass
    }

    public Long getCurrentChatId() {
        Update current = updateEvent.get();
        if (current.hasMessage()) {
            return current.getMessage().getChatId();
        }

        if (current.hasCallbackQuery()) {
            var cq = current.getCallbackQuery();
            if (cq.getMessage() != null) {
                return cq.getMessage().getChatId();
            }
            return cq.getFrom().getId();
        }

        return null;
    }

    public Optional<String> getCurrentTelegramUsername() {
        Update current = updateEvent.get();
        if (current == null) {
            return Optional.empty();
        }
        if (current.hasMessage() && current.getMessage().getFrom() != null) {
            return Optional.ofNullable(current.getMessage().getFrom().getUserName())
                    .filter(username -> !username.isBlank());
        }
        if (current.hasCallbackQuery() && current.getCallbackQuery().getFrom() != null) {
            return Optional.ofNullable(current.getCallbackQuery().getFrom().getUserName())
                    .filter(username -> !username.isBlank());
        }
        return Optional.empty();
    }

    public String getMessageText() {
        return updateEvent.get().hasMessage() ? updateEvent.get().getMessage().getText() : "";
    }

    public boolean isMessageCommand() {
        return updateEvent.get().hasMessage() && updateEvent.get().getMessage().isCommand();
    }

    public String getCallbackQueryButtonKey() {
        return updateEvent.get().hasCallbackQuery() ? updateEvent.get().getCallbackQuery().getData() : "";
    }

    public Message sendTextMessage(String text) {
        return executeTelegramApiMethod(createSendMessage(text, null));
    }

    public Message sendHtmlMessage(String text) {
        return executeTelegramApiMethod(createSendMessage(text, PARSE_MODE_HTML));
    }

    public Message sendPhotoMessage(String photoKey) {
        return executeTelegramApiMethod(createApiPhotoMessageCommand(photoKey));
    }

    public void updateTextMessage(Message message, String text) {
        String safe = textTruncator.truncate(text);
        if (text != null && safe.length() < text.length()) {
            log.warn("Текст ответа обрезан с {} до {} символов (лимит Telegram {})",
                    text.length(), safe.length(), TelegramTextTruncator.TELEGRAM_MESSAGE_TEXT_MAX);
        }
        EditMessageText command = new EditMessageText();
        command.setChatId(message.getChatId());
        command.setMessageId(message.getMessageId());
        command.setText(safe);
        executeTelegramApiMethod(command);
    }

    public Message sendTextButtonsMessage(String text, String... buttons) {
        SendMessage command = createSendMessage(text, null);
        if (buttons.length > 0) {
            attachButtons(command, List.of(buttons));
        }
        return executeTelegramApiMethod(command);
    }

    public Message sendHtmlButtonsMessage(String text, String... buttons) {
        SendMessage command = createSendMessage(text, PARSE_MODE_HTML);
        if (buttons.length > 0) {
            attachButtons(command, List.of(buttons));
        }
        return executeTelegramApiMethod(command);
    }

    private SendMessage createSendMessage(String text, String parseMode) {
        SendMessage message = new SendMessage();
        message.setText(text);
        if (parseMode != null) {
            message.setParseMode(parseMode);
        }
        message.setChatId(getCurrentChatId());
        return message;
    }

    private void attachButtons(SendMessage message, List<String> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = 0; i < buttons.size(); i += 2) {
            String buttonName = buttons.get(i);
            String buttonValue = buttons.get(i + 1);

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonName);
            button.setCallbackData(buttonValue);

            keyboard.add(List.of(button));
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
    }

    private SendPhoto createApiPhotoMessageCommand(String photoKey) {
        InputFile inputFile = new InputFile();
        inputFile.setMedia(resourceLoader.loadImage(photoKey), photoKey);

        SendPhoto photo = new SendPhoto();
        photo.setPhoto(inputFile);
        photo.setChatId(getCurrentChatId());
        return photo;
    }

    private <R extends Serializable, A extends BotApiMethod<R>> R executeTelegramApiMethod(A apiMethod) {
        try {
            return super.sendApiMethod(apiMethod);
        } catch (TelegramApiException e) {
            throw new TelegramBotApiException("Не удалось выполнить запрос к Telegram API", e);
        }
    }

    private Message executeTelegramApiMethod(SendPhoto message) {
        try {
            return super.execute(message);
        } catch (TelegramApiException e) {
            throw new TelegramBotApiException("Не удалось отправить фото в Telegram", e);
        }
    }
}
