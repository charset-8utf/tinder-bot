package com.tinderbot.telegram.service.telegram;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCleaner implements IMessageCleaner {

    private final TelegramUiSessionStore telegramUi;

    @Override
    public void deleteAllMessages(Long chatId, MultiSessionTelegramBot bot) {
        Stream.concat(
                        Stream.of(
                                telegramUi.getPhotoMessageId(chatId).orElse(null),
                                telegramUi.getWelcomeMessageId(chatId).orElse(null),
                                telegramUi.getCurrentMenuMessageId(chatId).orElse(null)
                        ),
                        telegramUi.getBotMessageIds(chatId).stream()
                )
                .filter(Objects::nonNull)
                .forEach(messageId -> deleteMessage(chatId, messageId, bot));
        telegramUi.clearBotMessageIds(chatId);
        telegramUi.clearMessageIds(chatId);
    }

    @Override
    public void deleteCurrentMenu(Long chatId, MultiSessionTelegramBot bot) {
        telegramUi.getCurrentMenuMessageId(chatId).ifPresent(id -> deleteMessage(chatId, id, bot));
        telegramUi.setCurrentMenuMessageId(chatId, null);
    }

    private void deleteMessage(Long chatId, Integer messageId, MultiSessionTelegramBot bot) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatId.toString());
        delete.setMessageId(messageId);
        try {
            bot.execute(delete);
            log.debug("Сообщение {} удалено в чате {}", messageId, chatId);
        } catch (TelegramApiException e) {
            log.warn("Не удалось удалить сообщение {}: {}", messageId, e.getMessage());
        }
    }
}
