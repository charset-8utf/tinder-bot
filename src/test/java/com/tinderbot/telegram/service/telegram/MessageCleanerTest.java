package com.tinderbot.telegram.service.telegram;

import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageCleanerTest {

    @Mock
    private ISessionService sessionService;

    @Mock
    private MultiSessionTelegramBot bot;

    @InjectMocks
    private MessageCleaner messageCleaner;

    private final long chatId = 123L;

    @Test
    void deleteAllMessages_shouldDeleteAllStoredMessages() throws TelegramApiException {
        when(sessionService.getPhotoMessageId(chatId)).thenReturn(Optional.of(101));
        when(sessionService.getWelcomeMessageId(chatId)).thenReturn(Optional.of(102));
        when(sessionService.getCurrentMenuMessageId(chatId)).thenReturn(Optional.of(103));
        when(sessionService.getBotMessageIds(chatId)).thenReturn(List.of(104, 105));

        messageCleaner.deleteAllMessages(chatId, bot);

        verify(bot, times(5)).execute(any(DeleteMessage.class));
        verify(sessionService).clearBotMessageIds(chatId);
        verify(sessionService).clearMessageIds(chatId);
    }

    @Test
    void deleteAllMessages_shouldSkipMissingIds() throws TelegramApiException {
        when(sessionService.getPhotoMessageId(chatId)).thenReturn(Optional.empty());
        when(sessionService.getWelcomeMessageId(chatId)).thenReturn(Optional.of(102));
        when(sessionService.getCurrentMenuMessageId(chatId)).thenReturn(Optional.empty());
        when(sessionService.getBotMessageIds(chatId)).thenReturn(List.of(104));

        messageCleaner.deleteAllMessages(chatId, bot);

        verify(bot, times(2)).execute(any(DeleteMessage.class));
        verify(sessionService).clearBotMessageIds(chatId);
        verify(sessionService).clearMessageIds(chatId);
    }

    @Test
    void deleteCurrentMenu_shouldDeleteOnlyMenu() throws TelegramApiException {
        when(sessionService.getCurrentMenuMessageId(chatId)).thenReturn(Optional.of(103));

        messageCleaner.deleteCurrentMenu(chatId, bot);

        verify(bot, times(1)).execute(any(DeleteMessage.class));
        verify(sessionService).setCurrentMenuMessageId(chatId, null);
    }

    @Test
    void deleteCurrentMenu_shouldDoNothingIfMenuIdMissing() throws TelegramApiException {
        when(sessionService.getCurrentMenuMessageId(chatId)).thenReturn(Optional.empty());

        messageCleaner.deleteCurrentMenu(chatId, bot);

        verify(bot, never()).execute(any(DeleteMessage.class));
        verify(sessionService).setCurrentMenuMessageId(chatId, null);
    }

    @Test
    void deleteAllMessages_shouldContinueIfOneDeleteFails() throws TelegramApiException {
        when(sessionService.getPhotoMessageId(chatId)).thenReturn(Optional.of(101));
        when(sessionService.getWelcomeMessageId(chatId)).thenReturn(Optional.of(102));
        when(sessionService.getCurrentMenuMessageId(chatId)).thenReturn(Optional.of(103));
        when(sessionService.getBotMessageIds(chatId)).thenReturn(List.of(104));

        when(bot.execute(any(DeleteMessage.class)))
                .thenThrow(new TelegramApiException("Network error"))
                .thenReturn(null);

        messageCleaner.deleteAllMessages(chatId, bot);

        verify(bot, times(4)).execute(any(DeleteMessage.class));
        verify(sessionService).clearBotMessageIds(chatId);
        verify(sessionService).clearMessageIds(chatId);
    }
}